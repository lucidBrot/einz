package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzPlayCardAction;
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
    private ThreadedEinzServer papi;
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

    public EinzServerClientHandler(Socket clientSocket, ThreadedEinzServer papi, ServerFunctionDefinition serverFunctionDefinition) {
        Log.d("EinzServerThread", "started");

        //<debug>
        Class c = EinzRegistrationParser.class; String mg = "registration";
        JSONObject container = new JSONObject();
        try {
            container.put("test", c);
            Log.d("DEBUG", container.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // D/DEBUG: {"test":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser"}
        //</debug>

        this.socket = clientSocket;
        this.serverInterface = serverFunctionDefinition;
        this.einzParserFactory = new EinzParserFactory();
        this.einzActionFactory = new EinzActionFactory(serverInterface);
        // TODO: initialize ParserFactory by registering all Messagegroup->Parser mappings
        //registerParserMappings();

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

    /**
     * Load the resource file containing an Array of Pair<String messagegroup, Class<\? extends EinzParser>
     *     @param rawResourceFile e.g. R.raw.serverDefaultParserMappings
     *                            This file should be formatted as a JSONObject containing a JSONArray "parsermappings" of JSONObjects of the form
     *                            {"messagegroup":"some thing", "mapstoparser":{...}}
     *                            where {...} stands for the JSON representation of the EinzParser class
     *     @throws JSONException if some of the JSON is not as expected
     *     @throws InvalidResourceFormatException if the mapping objects themselves are not valid. Contains more details in extended message
     */
    private void registerParserMappings(int rawResourceFile) throws JSONException, InvalidResourceFormatException {
        InputStream jsonStream = papi.applicationContext.getResources().openRawResource(rawResourceFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        JSONArray array = jsonObject.getJSONArray("parsermappings");
        int size = array.length();
        // register each object
        for(int i=0; i<size; i++){
            JSONObject pair = array.getJSONObject(i);
            Object o =pair.get("mapstoparser");
            if(! (o instanceof Class)) {
                throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"parsermappings\" is not of type Class");
            }

            Class<? extends EinzParser> parserclass = (Class<? extends EinzParser>) o; // TODO: how to handle unchecked generic casts?
            this.einzParserFactory.registerMessagegroup(pair.getString("messagegroup"), parserclass);
        }
    }

    // https://stackoverflow.com/questions/6774579/typearray-in-android-how-to-store-custom-objects-in-xml-and-retrieve-them
    // utility function
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
                    /* synchronized (socketWriteLock) {
                        out.writeBytes(line + "\r\n");
                        out.flush();
                    } */
                    sendMessage(line + "\r\n");
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
            }
            try {
                out.flush();
            } catch (IOException e) {
                Log.e("EinzServerThread","sendMessage: failed because of IOException 2 "+e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /*
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
                        break;
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

        Log.d("EinzServerThread/parse","YOU'RE STILL USING THE OLD PARSE FUNCTION.");
    }
    */

    private void parseMessage(String message){ // TODO: if message is register/deregister, make sure register/deregister is called
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
            // if action was not registered yet, it will be null
            if(einzAction != null) {
                einzAction.run();
            }
        } catch (JSONException e) {
            Log.e("EinzServerThread/parse", "JSON Error in parseMessage");
            e.printStackTrace();
        }

    }

    public String getConnectedUser() {
        return connectedUser;
    }

    public void setConnectedUser(String connectedUser) {
        this.connectedUser = connectedUser;
    }
}
