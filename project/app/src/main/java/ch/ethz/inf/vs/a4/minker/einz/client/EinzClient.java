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

    public EinzClient(String serverIP, int serverPort, Context appContext) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.appContext = appContext;
        this.actionCallbackInterface = new ClientMessengerCallback();
        this.clientMessenger = new ClientMessenger(appContext, this.actionCallbackInterface);
        this.connection = new EinzClientConnection(serverIP, serverPort, clientMessenger);

    }

    /**
     * Starts receiving(spinning) EinzClientConnection in background thread
     */
    @Override
    public void run() {
        this.clientConnectionThread = new Thread(this.connection);
        this.clientConnectionThread.start(); // establish connection

        // TODO: register and all other messages
        Log.d("EinzClient/run", "server is up methinks");

        // send messages in background because android does not allow networking in main thread
        (new Thread(new Runnable() {
            @Override
            public void run() {
                while (!connection.isConnected()) {
                    try {
                        sleep(1); // wait for server ready
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                // example message sending. implement this where you like
                EinzMessageHeader header = new EinzMessageHeader("registration", "Register");
                EinzRegisterMessageBody body = new EinzRegisterMessageBody("the real chris", "player"); // getting all the girls
                final EinzMessage<EinzRegisterMessageBody> message = new EinzMessage<>(header, body);

                //DEBUG

                connection.sendMessage(Debug.debug_getRegisterMessage("roger")); // TODO: use real username


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
