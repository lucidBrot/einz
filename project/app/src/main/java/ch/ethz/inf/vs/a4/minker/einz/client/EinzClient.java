package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.Debug;
import org.json.JSONException;

import static java.lang.Thread.sleep;

public class EinzClient implements Runnable {

    private final EinzClientConnection connection;
    private ClientActionCallbackInterface actionCallbackInterface; // handles the methods that an action might call (as reaction to incoming message)
    private ClientMessenger clientMessenger; // handles incoming messages
    private String serverIP;
    private int serverPort;
    private Context appContext;
    private Thread clientConnectionThread;
    private String username;
    private String role;

    public EinzClient(String serverIP, int serverPort, Context appContext, String username, String role) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.appContext = appContext;
        this.actionCallbackInterface = new ClientMessengerCallback();
        this.clientMessenger = new ClientMessenger(appContext, this.actionCallbackInterface);
        this.connection = new EinzClientConnection(serverIP, serverPort, clientMessenger);
        this.username = username;
        this.role = role;
    }

    /**
     * Starts receiving(spinning) EinzClientConnection in background thread
     */
    @Override
    public void run() {

        /*
        How to use this class:
            EinzClientConnection handles connecting to the server and receiving packets.
                It features a method sendMessage() which can be used from different threads to send a message to the server
            Once a Message is received, the corresponding parser is looked up through the parserFactory by checking the mappings
                specified in the files R.raw.initial_networking_parsing_mappings and R.raw.initial_game_logic_parser_mappings.
                These files are not only one file because you might want to separate the networking-related messaging with the game-logic related.
                However, you could technically specify any parser mapping in either of the files (If you specify it in both, we'll see what happens, I think it just keeps one)
            The thus found Parser takes the message and returns it as a Java Object with the content as variables.
            This goes through the actionFactory which is mapped in R.raw.client_initial_networking_action_mappings and R.raw.client_initial_game_logic_action_mappings
                those are separated from the server's action mappings because while the server does not receive different messages than those specified in these files,
                the reaction might be different.
            This action has a function run(). You should implement this function in the corresponding child of EinzAction, found in messageparsing.actiontypes
                Within this run function, you have access to an interface of type ClientActionCallbackInterface, which is implemented by ClientMessengerCallback,
                So you can implement any functionality there and update the interface, then use those functions in the class.
            This Client is instanciated once the server is up, called from lobbyactivity, if the device is hosting it, or when the user entered the address of the server and the port and confirmed if it's a client-only device.
            The LobbyListAction offers what the LobbyUIInterface provides to update the view. (Feel free to change that as well, the LobbyActivity is the only class implementing this)
            This client should implement some functions that allow the UI-thread to make this client send a message (in a different thread), e.g. when the UI realizes that the host wants to specifyRules or StartGame.
         */

        this.clientConnectionThread = new Thread(this.connection);
        this.clientConnectionThread.start(); // establish connection

        // TODO: register and all other messages
        Log.d("EinzClient/run", "server is up methinks"); // if server is running on localhost, it told us when it was ready to accept connections
        // still need to spin until isConnected to make sure we do not send register message before connecting, thus losing that message

        // send messages in background because android does not allow networking in main thread
         sendRegistrationMessage(); // might be sent twice, because the host automatically calls sendRegistrationMEssage when the server is ready to receive it.
        // but is probably not harmful and other clients might need to send from here because they don't know if the server is ready. Maybe they just need to sleep for 10ms after connecting and before registering
        // or we could implement a server-ready message.
    }

    /**
     * starts a new thread and sends the registration message from there. username and role as specified when this client was constructed.
     */
    public void sendRegistrationMessage(){
        (new Thread(new Runnable() {
            @Override
            public void run() {

                /*
                // the following bugfix is no longer needed because the server tells the first client that connected when it is ready for the register message
                // other clients are hopefully slow enough, else they would have to wait or get a response from the server for this... // TODO: (later) check whether non-host clients send register message too early
                //<Bugfix>
                while (!connection.isConnected()) { // spin until connected
                    //sleep(10);
                    // wait for server ready. it works if you put a breakpoint on the line with "new Thread(", so waiting should help
                    // this sleeping doesn't seem to help. sometimes it still doesn't get response of the server even after sleeping 1000000, or 10000. Seems to work with 1 and 10 ms though
                    // BUT: why is this the case? And why does it only sometimes work?
                    //      below sleep was added after this comment
                }

                // sleep a little after the connection is there, somehow this helps. If this is not there, the message is lost before the server is fully ready
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //</Bugfix>
                */

                // example message sending. implement this where you like
                EinzMessageHeader header = new EinzMessageHeader("registration", "Register");
                EinzRegisterMessageBody body = new EinzRegisterMessageBody(username, role); // getting all the girls
                final EinzMessage<EinzRegisterMessageBody> message = new EinzMessage<>(header, body);

                //DEBUG

                connection.sendMessage(message);


                //simple logging:
                try {
                    Log.d("EinzClient/run", "sent register message: " + message.toJSON().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }
}
