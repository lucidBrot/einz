package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzFinishRegistrationPhaseAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzPlayCardAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzRegisterAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzJsonMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;

/**
 * This class handles one Connection per instance (thread)
 */
public class EinzServerClientHandler implements Runnable{
    public Socket socket;

    public boolean spin = false;
    private ThreadedEinzServer parentEinzServer;
    private DataOutputStream out = null;
    public final Object socketWriteLock; // lock onto this for writing
    public final Object socketReadLock;
    private InputStream inp;
    private BufferedReader brinp;


    private ServerFunctionDefinition serverInterface; // used to call EinzActions
    private EinzParserFactory einzParserFactory; // reuse factories instead of recreating every time
    private EinzActionFactory einzActionFactory;

    // identify this connection by its user as soon as this is available
    private String connectedUser; // TODO: set connectedUser on register

    /**
     * Listens on {@param clientSocket} for incoming messages and provides an interface {@link ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler#sendMessage(String)} for sending to the client associated with this socket.
     * Stores the username of the connected user as it is registered in {@link this.connectedUser}
     * @param clientSocket socket to which a client connected
     * @param parentEinzServer the EinzServer instance creating this thread
     * @param serverFunctionDefinition the implementation of the interface to run the actions in.
     */
    public EinzServerClientHandler(Socket clientSocket, ThreadedEinzServer parentEinzServer, ServerFunctionDefinition serverFunctionDefinition) {
        Log.d("ESCH", "started new instance");

        this.parentEinzServer = parentEinzServer;
        parentEinzServer.incNumClients();

        debug_printJSONRepresentationOf(EinzRegistrationParser.class);

        this.socket = clientSocket;
        this.serverInterface = serverFunctionDefinition;
        this.einzParserFactory = new EinzParserFactory();
        this.einzActionFactory = new EinzActionFactory(serverInterface, this.parentEinzServer.getServerManager());

        // TODO: initialize ParserFactory by registering all Messagegroup->Parser mappings
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

        // TODO: initialize ActionFactory by registering all Message->Action mappings
        registerActionMappings();


        socketWriteLock = new Object();
        socketReadLock = new Object();

        // initialize socket stuff
        inp = null;
        brinp = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.e("ESCH", "Failed to initialize run(). Aborting");
            e.printStackTrace();
            return;
        }
    }

    /**
     * For debug purposes only, should not have side effects at all.
     * @param o
     */
    private void debug_printJSONRepresentationOf(Object o){
        //<debug>
        JSONObject container = new JSONObject();
        try {
            container.put("your thing:", o);
            Log.d("ESCH/DEBUG", "printJSONRepresentationOF() : "+ container.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // D/DEBUG: {"test":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser"}
        //</debug>
    }


    /**
     * load ParserMappings for networking and for gamelogic from file set up in {@link EinzServerManager}
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
    private void registerActionMappings(){
        this.einzActionFactory.registerMapping(EinzJsonMessageBody.class, EinzFinishRegistrationPhaseAction.class); // DEBUG purely. not actually useful// TODO: remove debug mappings
        this.parentEinzServer.getServerManager().loadAndRegisterNetworkingActions(this.einzActionFactory);
        this.parentEinzServer.getServerManager().loadAndRegisterGameLogicActions(this.einzActionFactory);
    }

    // source: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server

    @Override
    public void run() {
        ///Log.d("ESCH", "run() was called. Listening for messages");

        String line;
        spin = true;
        while (spin) {
            try {
                line = readSocketLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    parentEinzServer.decNumClients();
                    Log.d("ESCH", "closed clientSocket");
                    return;
                } else {
                    Log.d("ESCH", "received line: "+line);
                    runAction(parseMessage(line));
                    sendMessage(line + "\r\n"); // echo back the same packet
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("ESCH", "Something Failed. Probably the client disconnected without warning. Or maybe the socket is closed.");
                // TODO: inform that user has left if he was registered. and stop thread
                this.stopThread();
                this.onClientDisconnected();
                return;
            }
        }
    }

    // TODO: implement this method
    private void stopThread() {
        Log.d("ESCH/stopThread", "NOT YET IMPLEMENTED!");
    }

    // TODO implement this method
    private void onClientDisconnected(){
        Log.d("ESCH/clientDisconnected", "NOT YET IMPLEMENTED");
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
     * sends the message to the client associated with this EinzServerClientHandler instance.
     * Makes sure only one thread is concurrently writing to socket
     * @param message the line to send. Do not include \r\n except as the end of your package (as we're reading a packet each line)
     *                DO include it at the end of the package
     */
    public void sendMessage(String message) {
        if(out==null){
            Log.e("EinzServerThread", "sendMessage: Not yet fully initialized. cannot send message.");
        }
        synchronized(socketWriteLock){
            // maybe need to append  + "\r\n" to message ?
            try {
                out.writeBytes(message);
            } catch (IOException e) {
                Log.e("EinzServerThread","sendMessage: failed because of IOException "+e.getMessage());
                e.printStackTrace();

                this.onClientDisconnected();
                this.stopThread(); // is it sure that the client has disconnected or can this happen otherwise? I believe so
            }
            try {
                out.flush();
            } catch (IOException e) {
                Log.e("EinzServerThread","sendMessage: failed because of IOException 2 "+e.getMessage());
                e.printStackTrace();

                this.onClientDisconnected();
                this.stopThread(); // is it sure that the client has disconnected or can this happen otherwise? I believe so
            }
        }
    }

    /**
     * reads synchronizedly from socket
     * @return the line
     */
    private String readSocketLine() throws IOException {
        synchronized (socketReadLock) {
            return brinp.readLine();
        }
    }


    /**
     * Passes the message String into a parser, then translates the resulting EinzMessage into an action
     * @param message
     * @return might be null, e.g. if no action was registered yet.
     */
    private EinzAction parseMessage(String message){
        try {
            EinzParser einzParser = this.einzParserFactory.generateEinzParser(message);
            EinzMessage einzMessage = einzParser.parse(message); // TODO: implement parser, especially for when message is not valid

            //<Debug>
            /*EinzMessage einzMessage = new EinzMessage(
                    new EinzMessageHeader("test", "more test"),
                    new EinzPlayCardMessageBody()
            );
            this.einzActionFactory.registerMapping(einzMessage.getBody().getClass(), EinzPlayCardAction.class);*/
            //</Debug>

            EinzAction einzAction = this.einzActionFactory.generateEinzAction(einzMessage, connectedUser);
            return einzAction;
        } catch (JSONException e) {
            Log.e("EinzServerThread/parse", "JSON Error in parseMessage");
            e.printStackTrace();
        }
        return null;
    }

    public String getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(String connectedUser) {
        this.connectedUser = connectedUser;
    }
}
