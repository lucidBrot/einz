package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.EinzConstants;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.OnKeepaliveTimeoutCallback;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.SendMessageCallback;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Thread.sleep;

/**
 * This class handles one Connection per instance (thread)
 */
public class EinzServerClientHandler implements Runnable, SendMessageCallback{

    private PrintWriter bufferOut;
    public Socket socket;

    private boolean spin = false;
    private boolean stopping = false;
    private boolean firstConnectionOnServer = false; // whether this user should be considered admin

    private ThreadedEinzServer parentEinzServer;
    private DataOutputStream out = null;
    public ReentrantReadWriteLock socketLock;
    private ReentrantLock socketReadLock = new ReentrantLock();
    private ReentrantLock socketWriteLock = new ReentrantLock();
    private InputStream inp;
    private BufferedReader brinp;


    private ServerFunctionDefinition serverInterface; // used to call EinzActions
    private EinzParserFactory einzParserFactory; // reuse factories instead of recreating every a_time
    private EinzActionFactory einzActionFactory;

    // identify this connection by its user as soon as this is available
    private String connectedUser = null; // is set on register and never unset because when the client disconnects, this thread is stopped
    private String latestUser = null; // is only null if there was never a username

    KeepaliveScheduler keepaliveScheduler;

    /**
     * Listens on {@param clientSocket} for incoming messages and provides an interface {@link ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler#sendMessage(String)} for sending to the client associated with this socket.
     * Stores the username of the connected user as it is registered in {@link this.connectedUser}
     * @param clientSocket socket to which a client connected
     * @param parentEinzServer the EinzServer instance creating this thread
     * @param serverFunctionDefinition the implementation of the interface to run the actions in.
     */
    public EinzServerClientHandler(Socket clientSocket, ThreadedEinzServer parentEinzServer, ServerFunctionDefinition serverFunctionDefinition, boolean firstConnectionOnServer) {
        Log.d("ESCH", "started new instance");

        if(Debug.SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED > 0){
            try {
                Log.d("ESCH", "Sleeping for "+Debug.SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED+" ms.");
                sleep(Debug.SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED);
            } catch (InterruptedException e) {
                Log.d("Debug", "Was interrupted while sleeping for debug purposes");
                e.printStackTrace();
            }
        }
        Debug.a_time = System.currentTimeMillis() - Debug.a_startTime;

        this.parentEinzServer = parentEinzServer;
        parentEinzServer.incNumClients();
        this.firstConnectionOnServer = firstConnectionOnServer;

        this.socket = clientSocket;
        this.socketLock = new ReentrantReadWriteLock(true);
        this.serverInterface = serverFunctionDefinition;

        // initialize socket stuff
        // done this early to make sure it's soon-ishly able to buffer
        inp = null;
        brinp = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp, EinzConstants.ENCODING));
            bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), EinzConstants.ENCODING)), true);
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.e("ESCH", "Failed to initialize run(). Aborting");
            e.printStackTrace();

        }

        Debug.a_endTime = System.currentTimeMillis() - Debug.a_startTime; // about 180 ms
        long time_until_constructor_called = Debug.a_time;
        long time_from_constructor_until_socket_up = Debug.a_endTime - time_until_constructor_called; // about 10 ms

        this.einzParserFactory = new EinzParserFactory();
        this.einzActionFactory = new EinzActionFactory(serverInterface, this.parentEinzServer.getServerManager(), this);

        try {
            registerParserMappings();
        } catch (JSONException e) {
            Log.e("ESCH/rParserMappings", "failed to initialize ParserFactory by loading from resource file.");
            e.printStackTrace();
        } catch (InvalidResourceFormatException e) {
            Log.e("ESCH/rParserMappings", "failed to initialize ParserFactory by loading from resource file. InvalidResourceFormatException: "+e.getExtendedMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e("ESCH/rParserMappings", "failed to initialize ParserFactory by loading from resource file because at least one class wasnt defined: "+e.getMessage());
            e.printStackTrace();
        }


        try {
            registerActionMappings();
        } catch (InvalidResourceFormatException e) {
            Log.e("ESCH/rActionMappings", "failed to initialize ActionFactory by loading from resource file.");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("ESCH/rActionMappings", "failed to initialize ActionFactory by loading from resource file. InvalidResourceFormatException");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            Log.e("ESCH/rActionMappings", "failed to initialize ActionFactory by loading from resource file. ActionMappings");
            e.printStackTrace();
        }

        this.keepaliveScheduler = new KeepaliveScheduler(this, new OnKeepaliveTimeoutCallback() {
            @Override
            public void onKeepaliveTimeout() {
                Log.d("keepalive", "user lost connection");
                onClientDisconnected(); // involves trying to tell that client that he disconnected
            }
        });
    }



    /**
     * load ParserMappings for networking and for gamelogic from file set up in {@link EinzServerManager}
     * Should be fine to call without any synchronization as long as this thread is the only one using {@link #einzParserFactory}
     * @throws JSONException
     * @throws InvalidResourceFormatException
     * @throws ClassNotFoundException
     */
    private void registerParserMappings() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        this.parentEinzServer.getServerManager().loadAndRegisterNetworkingParsers(this.einzParserFactory);
        this.parentEinzServer.getServerManager().loadAndRegisterGameLogicParsers(this.einzParserFactory);
    }


    /**
     * load ActionMappings for networking and for gamelogic from file set up in {@link EinzServerManager}
     */
    private void registerActionMappings() throws InvalidResourceFormatException, JSONException, ClassNotFoundException {
        ///this.einzActionFactory.registerMapping(EinzJsonMessageBody.class, EinzFinishRegistrationPhaseAction.class); // DEBUG purely. not actually useful
        this.parentEinzServer.getServerManager().loadAndRegisterNetworkingActions(this.einzActionFactory);
        this.parentEinzServer.getServerManager().loadAndRegisterGameLogicActions(this.einzActionFactory);
    }

    // source: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server

    @Override
    public void run() {

        String line;
        spin = true;
        boolean firstround = true;
        this.keepaliveScheduler.runInParallel(); // run timeout timers in background

        while (spin) {
            try {
                socketReadLock.lock();
                if(isFirstConnectionOnServer() && firstround){parentEinzServer.firstESCHReady(); firstround=false;} // inform first(host probably) client when the server is ready to receive the register message
                line = readSocketLine();
                socketReadLock.unlock();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socketWriteLock.lock();
                    socket.close();
                    socketWriteLock.unlock();
                    Log.d("ESCH", "user sent QUIT or null. Closing connection...");
                    onClientDisconnected();
                    stopThreadPatiently();
                    Log.d("ESCH", "closed clientSocket");
                    return;
                } else {
                    if(Debug.logKeepaliveSpam || !line.equals("{\"header\":{\"messagegroup\":\"networking\",\"messagetype\":\"KeepAlive\"},\"body\":{}}")){
                        Log.d("ESCH", "received line: "+line);
                    }
                    this.keepaliveScheduler.onAnyMessageReceived();
                    if(parentEinzServer.DEBUG_ECHO) sendMessage("Your Package was: "+line + "\r\n"); // echo back the same packet // T/ODO: don't echo back
                    runAction(parseMessage(line));

                }

            } catch (IOException e) {
                if(spin){
                e.printStackTrace();
                Log.w("ESCH", "Something Failed. Probably the client disconnected without warning. Or maybe the socket is closed.");}
                else{
                    Log.d("ESCH", "IOException but it's fine because I'm supposed to stop anyways.");
                }
                return;
            }
        }

        this.onClientDisconnected();
        this.stopThreadPatiently();
        this.onThreadEnded();
    }

    private void onThreadEnded() {
        Log.d("ESCH/"+getLatestUser(), "Thread ending...");
        parentEinzServer.removeEinzServerClientHandlerFromClientHandlerList(this);
        Log.d("ESCH/"+getLatestUser(), "This should be the last you heard of this client handler thread");
    }

    /**
     * Make thread stop listening for incoming connections and close the socket. All queued messages from other threads might be dismissed and have to catch the IOException.
     */
    public void stopThreadPatiently() {
        if(stopping&&!spin) // all is being done already
            return;


        this.stopping = true;
        try {
            sleep(EinzConstants.SERVER_SLEEP_TIME_BETWEEN_STOP_LISTENING_AND_CLOSE_SOCKET_ON_SHUTDOWN);
        } catch (InterruptedException e) {
            Log.e("ESCH/stopPatiently", "You interrupted my sleep (giving the other threads a_time to finish their actions): ");
            e.printStackTrace();
        }
        socketWriteLock.lock();
        String usr = getLatestUser(); usr = (usr==null)?"has never been set":usr;
        Log.d("ESCH/stopThread", "STOPPING THREAD(user="+usr+") PATIENTLY!");
        this.spin = false;
        //close socket to avoid memory leak but don't care if it fails
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // what happens if other threads still want to write on this: probably IOException
        socketWriteLock.unlock();
    }


    /**
     * Unregisters user
     */
    private void onClientDisconnected(){
        if(!stopping && spin) {
            parentEinzServer.getServerManager().unregisterUser(latestUser, "timeout", "server");
            parentEinzServer.decNumClients();
        } else {
            parentEinzServer.decNumClients();
        }
        keepaliveScheduler.onShuttingDown();
    }

    /**
     * runs einzAction if it is not null. Else, does nothing
     */
    private void runAction(EinzAction einzAction){
        // if action was not registered yet, it will be null
        if(einzAction != null) {
            einzAction.run();
        } else {
            Log.i("ESCH/runAction", "Action was null");
        }
    }

    /**
     * sends the message to the client associated with this EinzServerClientHandler instance.<br>
     * Threadsafety: Writelock on {@link #socketWriteLock}<br>
     * @param message the line to send. Do not include \r\n except as the end of your package (as we're reading a packet each line)
     *                DO include it at the end of the package
     */
    public void sendMessage(String message) {

        if(out==null){
            Log.e("EinzServerThread", "sendMessage: Not yet fully initialized. cannot send message.");
        }

        socketWriteLock.lock(); //synchronized
            // maybe need to append  + "\r\n" to message ?
            bufferOut.print(message);
            bufferOut.flush();
            /* // old code that broke encoding
            try {
                //out.writeBytes(message); // makes ö fail
                //out.writeChars(message); // client receives gibberish message
                out.writeUTF(message); // message works with ö, but starts with ��x which cannot be translated to json...
                out.flush();
            } catch (IOException e) {
                if(getConnectedUser()!=null) { // didn't realize that user disconnected
                    Log.e("EinzServerThread", "sendMessage: failed because of IOException. Message was '" + message + "',\nIOException was: " + e.getMessage());
                    e.printStackTrace();

                    this.onClientDisconnected();
                    this.stopThreadPatiently(); // is it sure that the client has disconnected or can this happen otherwise? I believe so
                }else{
                    // user disconnected on purpose. just ignore what he would receive and let this thread die
                    if(!stopping){
                        this.stopThreadPatiently();
                    }
                }
            }
            */

        socketWriteLock.unlock();

        this.keepaliveScheduler.onAnyMessageSent();
    }

    /**
     * Same as the other sendMessage, but transforms JSON to String for you.<br/>
     * <b>appends \r\n at the end if there is no \n at the end</b>
     * Threadsafe ✔ <br/>
     * Sends message to the client who is connected to this {@link EinzServerClientHandler} Instance
     * @see #sendMessage(String)
     * @param message
     */
    public void sendMessage(JSONObject message){
        String msg = message.toString();
        if(!msg.endsWith("\n")){
            msg += "\r\n";
        }
        sendMessage(msg);
    }

    /**
     * Same as the other sendMessage, but transforms EinzMessage to JSON to String for you.<br>
     * <b>appends \r\n at the end if there is no \n at the end</b>
     * Threadsafe ✔<br>
     * Sends message to the client who is connected to this {@link EinzServerClientHandler} Instance
     * @see #sendMessage(JSONObject)
     * @see #sendMessage(String)
     * @param message
     */
    public void sendMessage(EinzMessage<? extends EinzMessageBody> message){
        try {
            sendMessage(message.toJSON());
        } catch (JSONException e) {
            Log.e("ESCH/sendMsg", "You sent an EinzMessage which could not be translated toJSON().");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * reads synchronously from socket
     * @return the line
     */
    private String readSocketLine() throws IOException {
        socketReadLock.lock();
            String ret = brinp.readLine();
        socketReadLock.unlock();
        return ret;
    }


    /**
     * Passes the message String into a parser, then translates the resulting EinzMessage into an action
     * @param message
     * @return might be null, e.g. if no action was registered yet.
     */
    private EinzAction parseMessage(String message){
        try {
            EinzParser einzParser = this.einzParserFactory.generateEinzParser(message);
            EinzMessage<? extends EinzMessageBody> einzMessage = einzParser.parse(message);

            //<Debug>
            /*EinzMessage einzMessage = new EinzMessage(
                    new EinzMessageHeader("test", "more test"),
                    new EinzPlayCardMessageBody()
            );
            this.einzActionFactory.registerMapping(einzMessage.getBody().getClass(), EinzPlayCardAction.class);*/
            //</Debug>

            EinzAction einzAction = this.einzActionFactory.generateEinzAction(einzMessage, getConnectedUser());
            return einzAction;
        } catch (JSONException e) {
            Log.w("ESCH/parse", "JSON Error in parseMessage");
            e.printStackTrace();
        }
        return null;
    }

    public String getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(String connectedUser) {
        this.connectedUser = connectedUser;
        if(!(connectedUser==null)){
            latestUser=connectedUser;
        }
    }

    public boolean isFirstConnectionOnServer() {
        return firstConnectionOnServer;
    }

    /**
     * @return the latest username associated with this thread. only null if there is none
     */
    public String getLatestUser() {
        return latestUser;
    }

    public ReentrantLock getSocketReadLock() {
        return socketReadLock;
    }

    public ReentrantLock getSocketWriteLock() {
        return socketWriteLock;
    }
}
