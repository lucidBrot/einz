package ch.ethz.inf.vs.a4.minker.einz.client;

import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class EinzClientConnection {

    private final String serverIP;
    private final int serverPort;
    private boolean spin; // while this is true (and the read call is blocking), the client will wait for incoming messages

    // used to send messages
    private PrintWriter bufferOut;
    // used to read messages from the server
    private BufferedReader bufferIn;
    private EinzClientConnection.OnMessageReceived mMessageListener = null; // interface on message received

    public EinzClientConnection(String serverIP, int serverPort, EinzClientConnection.OnMessageReceived messageListener){
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.mMessageListener = messageListener;
    }

    public EinzClientConnection(String serverIP, int serverPort){
        this(serverIP, serverPort, null);
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
        if (bufferOut != null && !bufferOut.checkError()) {
            bufferOut.println(message);
            bufferOut.flush();
        }
    }

    public void run() {

        spin = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            Log.d("EinzClientConnection", "Connecting to "+serverIP +":"+serverPort);

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, serverPort);

            try {

                //sends the message to the server
                bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //receives the message which the server sends back
                bufferIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String mServerMessage;
                //in this while the client listens for the messages sent by the server
                while (spin) {
                    mServerMessage = bufferIn.readLine();

                    if (mServerMessage != null && mMessageListener != null) {
                        // call the message handler
                        mMessageListener.messageReceived(mServerMessage);
                    }

                    // TODO: method to stop the client
                }

            } catch (Exception e) {

                Log.e("EinzClientConnection", "Clientside Error.");
                e.printStackTrace();
                // TODO: handle these

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("EinzClientConnection", "Clientside Error (2)");
            e.printStackTrace();
            // TODO: handle these

        }

    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        spin = false;

        if (bufferOut != null) {
            bufferOut.flush();
            bufferOut.close();
        }

        mMessageListener = null;
        bufferIn = null;
        bufferOut = null;

        // TODO: is it ok to abort like this?
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
