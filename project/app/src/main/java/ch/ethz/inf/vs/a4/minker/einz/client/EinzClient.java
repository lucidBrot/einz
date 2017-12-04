package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Globals;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.OnKeepaliveTimeoutCallback;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKickMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.Debug;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;
import org.json.JSONException;

import static java.lang.Thread.sleep;

public class EinzClient implements Runnable {

    private final EinzClientConnection connection;
    private final boolean isHost; // true if admin
    private ClientActionCallbackInterface actionCallbackInterface; // handles the methods that an action might call (as reaction to incoming message)
    private ClientMessenger clientMessenger; // handles incoming messages
    private String serverIP;
    private int serverPort;
    private Context appContext;
    private Thread clientConnectionThread;
    /**
     * true if the client was shutdown (but apparently still exists)
     */
    private boolean dead;
    public KeepaliveScheduler keepaliveScheduler;

    public String getUsername() {
        return username;
    }

    private String username;
    private String role;
    private final LobbyUIInterface lobbyUI;

    /**
     * Creates a Client which offersa run() function. This function will establish a connection to the server, doing so in a new thread. For this, it is neccessary to run that function in a new thread in case waiting for connection takes forever.
     * This class only implements Runnable because it can, not because it must.
     * @param serverIP the IP to connect to
     * @param serverPort the Port to connect to
     * @param appContext the ApplicationContext from getApplicationContext()
     * @param username the desired username. The server might respond with a registerFailure though
     * @param role the desired role. Currently this could be "spectator" or "player"
     * @param isHost an indicator whether the server is running on this device.
     *               The first client to connect to the server is the admin. We hope that this will be consistently the same device, because of the network delay.
     *               isHost is only used to decide when to send the registration message
     * @param lobbyUI the implementation of{@link LobbyUIInterface} that should be called to update the UI
     */
    public EinzClient(String serverIP, int serverPort, Context appContext, String username, String role, boolean isHost, LobbyUIInterface lobbyUI) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.appContext = appContext;
        this.lobbyUI = lobbyUI;
        this.actionCallbackInterface = new ClientMessengerCallback(lobbyUI, appContext, this);
        this.clientMessenger = new ClientMessenger(appContext, this.actionCallbackInterface, this);
        this.connection = new EinzClientConnection(serverIP, serverPort, clientMessenger, this);
        this.keepaliveScheduler= new KeepaliveScheduler(EinzClient.this.connection, new OnKeepaliveTimeoutCallback() {
            @Override
            public void onKeepaliveTimeout() {
                EinzClient.this.connection.onKeepaliveTimeout();
            }
        });
        this.username = username;
        this.role = role;
        this.isHost = isHost;
        this.dead=false;
        Log.d("EinzClient", "Finished constructing this instance ("+username+")");
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

        // <debug>
        // fake receiving new lobby list
        //debug_faceReceiveUpdateLobbyList();
        //</debug>

        this.clientConnectionThread = new Thread(this.connection);
        this.clientConnectionThread.start(); // establish connection
        Log.d("EinzClient/run", "initiating connection in background");

        if(this.isHost) {
            Log.d("EinzClient/run", "server is up methinks"); // if server is running on localhost, it told us when it was ready to accept connections
            // still need to spin until isConnected to make sure we do not send register message before connecting, thus losing that message
        }

        this.keepaliveScheduler.runInParallel(); // run the timeout timers in background

        // send messages in background because android does not allow networking in main thread
         if(!isHost){ // if the server runs on the same device, it will tell the client when it is ready to receive the registrationmessage, and will execute onServersideHandlerReady
             spinUntilConnectedAndSleep();
             sendRegistrationMessage();
         }

        // TODO: all other messages
    }

    private void debug_fakeReceiveUpdateLobbyList() {
        this.clientMessenger.messageReceived("{\"header\":{\"messagegroup\":\"registration\",\"messagetype\":\"UpdateLobbyList\"},\"body\":{\"lobbylist\":{\""+username+"\":\""+role+"\"},\"admin\":\"this is a debug packet\"}}");
    }

    private void debug_fakeReceiveRegisterSuccess() {
        this.clientMessenger.messageReceived("{\"header\":{\"messagegroup\":\"registration\",\"messagetype\":\"RegisterSuccess\"},\"body\":{\"role\":\""+this.role+"\",\"username\":\""+this.username+"\"}}");
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
                // other clients are hopefully slow enough, else they would have to wait or get a response from the server for this...
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
                Log.d("EinzClient", "Trying to...");
                connection.sendMessageRetryXTimes(5,message);


                //simple logging:
                try {
                    Log.d("EinzClient/run", "...send(/t) register message: " + message.toJSON().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        })).start();
    }

    /**
     * this only spins until the socket is designated connected. The tcp connection might still not be initiated.
     * This function is currently not needed if you're the host, because the server will then inform you via callback when it is ready.
     */
    private void spinUntilConnectedAndSleep(){
        //<Bugfix>
        while (!connection.isConnected()) { // spin until connected. sadly, this can only tell us that the socket is connected locally, not that the client actually is connected
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // wait for server ready. it works if you put a breakpoint on the line with "new Thread(", so waiting should help
            // this sleeping doesn't seem to help. sometimes it still doesn't get response of the server even after sleeping 1000000, or 10000. Seems to work with 1 and 10 ms though
            // BUT: why is this the case? And why does it only sometimes work?
            //      below sleep was added after this comment
        }
        Log.d("EinzClient", "socket thinks it is connected");


        // sleep a little after the connection is there, somehow this helps. If this is not there, the message is lost before the server is fully ready
        // this helps because above while loop ending does not mean that the server is ready, only that the connection is said to exist when socket.connect() has been called
        // EDIT: that's kinda wrong. it might also be the client who has not yet initialized the buffer, but that was a bug in checking if it was null
        if(Debug.CLIENT_SLEEP_AFTER_CONNECTION_ESTABLISHED) { // for debugging, no longer needed because of sendMessageRetryXTimes instead.
            try {
                sleep(Globals.CLIENT_WAIT_TIME_AFTER_CONNECTION_ESTABLISHED);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //</Bugfix>
    }

    /**
     * Called by the Activity once the server stated that not only is it up and running {@link ServerActivityCallbackInterface#onLocalServerReady()}},
     * but also that it is ready to handle the first connected client. This happens after the client established a connection and the server initialized an {@link ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler}.
     * That means that the server calls {@link ServerActivityCallbackInterface#onFirstESCHReady()} on the host. The {@link ch.ethz.inf.vs.a4.minker.einz.UI.LobbyActivity} implements this interface and will inform the host client by calling this method.
     * From then on, the client is allowed to send messages freely.
     */
    public void onServersideHandlerReady(){
        sendRegistrationMessage();
    }

    /**
     * call this in an non-main-thread
     * @param unregisterFirst true if the client should first try to unregister
     */
    public void shutdown(boolean unregisterFirst) {
        if(unregisterFirst &&!this.dead) {sendUnregisterRequest();}
        this.connection.stopClient();
        this.dead = true;
    }

    private void sendUnregisterRequest() {
        EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterRequest");
        EinzUnregisterRequestMessageBody body = new EinzUnregisterRequestMessageBody(this.username); // getting all the girls
        final EinzMessage<EinzUnregisterRequestMessageBody> message = new EinzMessage<>(header, body);
        this.connection.sendMessageIgnoreFailures(message);
        Log.d("EinzClient/shutdown", "sent unregister message for "+this.username);
    }

    public void sendKickRequest(String username) {
        EinzMessageHeader header = new EinzMessageHeader("registration", "Kick");
        EinzKickMessageBody body = new EinzKickMessageBody(username);
        final EinzMessage<EinzKickMessageBody> message = new EinzMessage<>(header, body);
        this.connection.sendMessageRetryXTimes(5, message);
        Log.d("EinzClient/kick", "Sent kick request (kick "+username+")");
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * when the clientconnection stops completely after {@link EinzClientConnection#stopClient()}
     */
    void onClientConnectionDead() {
        this.dead=true;
    }
}
