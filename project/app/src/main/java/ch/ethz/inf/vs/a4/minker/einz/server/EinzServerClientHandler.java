package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;

/**
 * This class handles one Connection per instance (thread)
 */
public class EinzServerClientHandler implements Runnable{
    public Socket socket;

    public boolean spin = false;
    private ThreadedEinzServer papi;
    private DataOutputStream out = null;
    public final Object socketWriteLock; // lock onto this for writing
    public final Object socketReadLock;
    private InputStream inp;
    private BufferedReader brinp;


    private ServerFunctionDefinition serverInterface; // used to call EinzActions
    private EinzParserFactory einzParserFactory = new EinzParserFactory(); // reuse factories instead of recreating every time
    private EinzActionFactory einzActionFactory = new EinzActionFactory(serverInterface);

    // identify this connection by its user as soon as this is available
    private Player connectedUser; // TODO: set connectedUser on register

    public EinzServerClientHandler(Socket clientSocket, ThreadedEinzServer papi, ServerFunctionDefinition serverFunctionDefinition) {
        Log.d("EinzServerThread", "started");
        this.socket = clientSocket;
        this.serverInterface = serverFunctionDefinition;
        this.papi = papi;
        papi.incNumClients();

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
            Log.e("EinzServerThread", "Failed to initialize run(). Aborting");
            e.printStackTrace();
            return;
        }
    }

    // source: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server

    @Override
    public void run() {
        Log.d("EinzServerThread", "run() was called. Listening for messages");

        String line; // TODO: don't just echo the same thing back
        spin = true;
        while (spin) {
            try {
                synchronized (socketReadLock) {
                    line = brinp.readLine();
                }
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    papi.decNumClients();
                    Log.d("EinzServerThread", "closed clientSocket");
                    return;
                } else {
                    Log.d("EinzServerThread", "received line: "+line);
                    parseMessage(line);
                    synchronized (socketWriteLock) {
                        out.writeBytes(line + "\r\n");
                        out.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("EinzServerThread", "Something Failed");
                return;
            }
        }
    }

    /**
     * sends the message to the client associated with this EinzServerClientHandler instance.
     * Makes sure only one thread is concurrently writing to socket
     * @param message the line to send. Do not include \r\n as we're reading a packet each line.
     */
    public void sendMessage(String message) {
        if(out==null){
            Log.e("EinzServerThread", "sendMessage: Not yet fully initialized. cannot send message.");
        }
        synchronized(socketWriteLock){
            // maybe need to append  + "\n\r" to message ?
            try {
                out.writeBytes(message);
            } catch (IOException e) {
                Log.e("EinzServerThread","sendMessage: failed because of IOException "+e.getMessage());
                e.printStackTrace();
            }
            try {
                out.flush();
            } catch (IOException e) {
                Log.e("EinzServerThread","sendMessage: failed because of IOException 2 "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void parseMessage(String message){
        // check if valid JSON Object
        JSONObject msg = null;
        try {
            msg = new JSONObject(message);
        } catch (JSONException e) {
            // not a valid JSON Object
            Log.w("EinzServerThread/parse", "Received message that isn't a JSON Object: "+message);
        }

        if (msg != null){ // if JSON
            try {
                switch (msg.getString("messagetype")){
                    case "play card":
                        break; // TODO: all cases
                    case "debug message":
                        Log.d("EinzServerThread/parse", "Received debug message");
                        Log.d("EinzServerThread/parse", "\twith values "+msg.getJSONArray("val").toString());
                        break;
                    default:
                        Log.w("EinzServerThread/parse", "Received JSON message without messagetype as String");
                }
            } catch (JSONException e) {
                Log.w("EinzServerThread/parse", "Valid JSON but invalid messagetype");
                //e.printStackTrace();
            }
        }
    }

    private void parseMessageNew(String message){
        try {
            EinzParser einzParser = this.einzParserFactory.generateEinzParser(message);
            EinzMessage einzMessage = einzParser.parse(message);
            EinzAction einzAction = this.einzActionFactory.generateEinzAction(einzMessage);
            einzAction.run(connectedUser);
        } catch (JSONException e) {
            Log.e("EinzServerThread/parse", "JSON Error in parseMessageNew");
            e.printStackTrace();
        }

    }

    public Player getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(Player connectedUser) {
        this.connectedUser = connectedUser;
    }
}
