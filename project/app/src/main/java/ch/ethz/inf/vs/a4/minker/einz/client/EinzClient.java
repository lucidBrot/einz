package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import org.json.JSONException;

public class EinzClient implements Runnable {

    private EinzClientConnection connection;
    private ClientActionCallbackInterface actionCallbackInterface; // handles the methods that an action might call (as reaction to incoming message)
    private ClientMessenger clientMessenger; // handles incoming messages
    private String serverIP;
    private int serverPort;
    private Context appContext;

    public EinzClient(String serverIP, int serverPort, Context appContext){
        this.serverIP=serverIP;
        this.serverPort=serverPort;
        this.appContext=appContext;
        this.actionCallbackInterface = new ClientMessengerCallback();
        this.clientMessenger = new ClientMessenger(appContext, this.actionCallbackInterface);
        this.connection = new EinzClientConnection(serverIP, serverPort, clientMessenger);

    }

    @Override
    public void run() {
        this.connection.run(); // establish connection
        // TODO: register and all other messages
        Log.d("EinzClient/run", "connection established methinks");
        // example message sending. implement this where you like
        EinzMessageHeader header = new EinzMessageHeader("registration", "register");
        EinzRegisterMessageBody body = new EinzRegisterMessageBody("the real chris", "player"); // getting all the girls
        EinzMessage<EinzRegisterMessageBody> message = new EinzMessage<>(header, body);
        this.connection.sendMessage(message);

        //simple logging:
        try {
            Log.d("EinzClient/run", "sent register message: "+message.toJSON().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
