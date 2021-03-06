package ch.ethz.inf.vs.a4.minker.einz.client;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.EinzConstants;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.SendMessageCallback;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

/**
 * call {@link #run} to actually connect
 */
public class EinzClientConnection implements Runnable, SendMessageCallback {

    private final String serverIP;
    private final int serverPort;
    private boolean spin; // while this is true (and the read call is blocking), the client will wait for incoming messages

    // used to send messages
    private PrintWriter bufferOut;
    // used to read messages from the server
    private BufferedReader bufferIn;
    private Object bufferMonitor = new Object(); // used to synchronize any accesses to the buffers
    private Socket socket;
    private EinzClientConnection.OnMessageReceived mMessageListener = null; // interface on message received
    private EinzClient parentClient;

    /**
     * @param serverIP
     * @param serverPort
     * @param messageListener to react to messages. implement EinzClientConnection.OnMessageReceived
     */
    public EinzClientConnection(String serverIP, int serverPort, EinzClientConnection.OnMessageReceived messageListener, EinzClient parentClient) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.mMessageListener = messageListener;
        this.parentClient = parentClient;

    }

    void onKeepaliveTimeout() {
        //  onKeepaliveTimeout inform user that we lost connection
        this.parentClient.getActionCallbackInterface().onKeepaliveTimeout();
    }

    public EinzClientConnection(String serverIP, int serverPort, EinzClient parentClient) {
        this(serverIP, serverPort, null, parentClient);
    }

    /**
     * Sends the message entered by client to the server
     * <code>synchronized</code> because otherwise {@link #stopClient()} could interfere.
     *
     * @param message text entered by client
     */
    public synchronized void sendMessage(String message) throws SendMessageFailureException {
        if (bufferOut != null && !bufferOut.checkError()) {
            synchronized (bufferMonitor) {
                bufferOut.println(message);
                bufferOut.flush();
            }
            this.parentClient.keepaliveScheduler.onAnyMessageSent();
        } else {
            Log.w("ClientConnection", "bufferOut was not available to send message "+message);
            throw new SendMessageFailureException("OutputBuffer was not ready to send the message. Please retry again when you are positive that it is not null (or has errors)");
        }
    }

    /**
     * Same as the other sendMessage, but transforms JSON to String for you.<br/>
     * <b>appends \r\n at the end if there is no \n at the end</b>
     * Threadsafe ✔ <br/>
     * Sends message to the client who is connected to this {@link EinzServerClientHandler} Instance
     *
     * @param message
     * @see #sendMessage(String)
     */
    public synchronized void sendMessage(JSONObject message) throws SendMessageFailureException {
        String msg = message.toString();
        // don't add \r\n because println
        sendMessage(msg);
    }

    /**
     * Same as the other sendMessage, but transforms EinzMessage to JSON to String for you.<br>
     * (Does not append \r\n at the end if there is no \n at the end because we send this as a line anyways)
     * Threadsafe ✔<br>
     * Sends message to the client who is connected to this {@link EinzServerClientHandler} Instance
     *
     * @param message
     * @see #sendMessage(JSONObject)
     * @see #sendMessage(String)
     */
    public synchronized void sendMessage(EinzMessage message) throws SendMessageFailureException {
        try {
            sendMessage(message.toJSON());
        } catch (JSONException e) {
            Log.e("ESCH/sendMsg", "You sent an EinzMessage which could not be translated toJSON().");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // not synchronized. that's the whole point. Catch exceptions.
    public void run() {

        spin = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            Log.d("EinzClientConnection", "Connecting to " + serverIP + ":" + serverPort);

            //create a socket to make the connection with the server
            socket = new Socket(serverAddr, serverPort); // TODO: set timeout on the connection establishing itself
                                                        // see https://stackoverflow.com/a/4969788/2550406

            try {

                //sends the message to the server
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), EinzConstants.ENCODING)), true);

                //receives the message which the server sends back
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), EinzConstants.ENCODING));

                String mServerMessage;

                //in this while the client listens for the messages sent by the server
                while (spin) {
                    mServerMessage = bufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        // call the message handler
                        mMessageListener.messageReceived(mServerMessage);
                    } else {
                        // not sure when this would happen
                        // When server dies this happens
                        Log.w("EinzClientConnection", "UNEXPECTED: message or listener was null. Stopping client.");
                        this.parentClient.getActionCallbackInterface().onKeepaliveTimeout();
                        stopClient();
                    }
                }

            } catch (Exception e) { // errors about keeping connection

                Log.e("EinzClientConnection", "Clientside Error.");
                e.printStackTrace();
                // TODO: handle these

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (ConnectException e) {
            // TODO: connection timeout when trying to establish it - inform user.
        }
        catch (Exception e) { // errors about establishing connection

            Log.e("EinzClientConnection", "Clientside Error (2)");
            e.printStackTrace();
            // TODO: handle these, e.g. ECONNREFUSED (server not reachable under that port and IP
            // or java.net.ConnectException: Connection timed out
            // and tell LobbyListActivity that

        }

    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {
        synchronized (bufferMonitor) {
            Log.d("ClientConnection/stop", "stopping listening");
            spin = false;

            if (bufferOut != null) {
                bufferOut.flush();
                bufferOut.close();
            }

            mMessageListener = null;
            bufferIn = null;
            bufferOut = null;

            if(this.parentClient!=null)
                this.parentClient.onClientConnectionDead();

        }
    }

    /**
     * Returns the connection state of the socket.
     * Note: Closing a socket doesn't clear its connection state, which means this method will return true for a closed socket (see isClosed()) if it was successfuly connected prior to being closed.
     */
    public boolean isConnected() {
        return (this.socket != null && this.socket.isConnected());


        // old version. bad.
        //return (this.socket == null || this.socket.isConnected());
    }

    public void sendMessageIgnoreFailures(EinzMessage<EinzUnregisterRequestMessageBody> message) {
        try {
            this.sendMessage(message);
        } catch (SendMessageFailureException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageRetryXTimes(int X, EinzMessage<? extends EinzMessageBody> message) {
        int oldX = X;
        while (X>0) { // try 5 times to send the message. Otherwise we failed
            try {
                sendMessage(message);
                break;
            } catch (SendMessageFailureException e) {
                Log.w("EinzClient", "failed to send register message (X="+X+")");
                try {
                    sleep(100);
                } catch (InterruptedException e1) {
                    // whatever
                }
                X--;
            }
        }
        if(!isConnected()){X=-1;}
        if(X<=0){
            // we failed to register because the message buffer out was still null or something like that, but after trying de facto waiting for 100*5 ms, it should have worked.
            Log.w("ClientConnection", "Retried "+oldX+" times and slept for a total of 500 ms but the buffer is still unable to send");
        }
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
