package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Waits for incoming TCP connections, handles each in a separate Thread, dispatches serverside logic if neccessary
 * This class spins. It is a Runnable, supposed to be run in its own thread.
 */
public class ThreadedEinzServer implements Runnable { // apparently, 'implements Runnable' is better than e'xtends thread': https://stackoverflow.com/questions/5853167/runnable-with-a-parameter
    private int PORT = 1337;
    private boolean shouldStopServer = false;
    private ServerSocket serverSocket;

    public ThreadedEinzServer(int PORT){
        this.PORT = PORT;
    }

    public ThreadedEinzServer(){
        this(1337);
    }

    @Override
    public void run(){
        boolean success = this.launch(); // spins. returns false if it fails and true if it finishes gracefully
    }

    /**
     * Launches the server and spins waiting for connections.
     * @return false if launching the server failed, true otherwise
     */
    private boolean launch(){
        Log.d("EinzServer/launch","launching Server");
        serverSocket = null;
        Socket socket = null;

        // code example: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            Log.e("EinzServer/launch", "IOException while creating serverSocket on Port "+PORT);
            Log.e("EinzServer/launch", "Error Message: "+e.getMessage());
            e.printStackTrace();
            return false;
        }

        while (!shouldStopServer){

            Thread t = new Thread(){
                @Override
                public void run() {
                    // DEBUG: start client
                    // temporary. please do not use in real code
                    Log.d("EinzServer/debug", "simulating client");
                    TempClient tc = new TempClient(new TempClient.OnMessageReceived() {
                        @Override
                        public void messageReceived(String message) {
                            Log.d("TempClient", "received message "+message);
                        }
                    });
                    Log.d("TempClient", "running main");
                    tc.run();
                    tc.sendMessage("test message");
                }
            };
            t.run();

            try {
                socket = serverSocket.accept();
                Log.d("EinzServer/launch", "new connection");
            } catch (SocketException e){
                Log.d("EinzServer/launch", "stopping accepting connections");
                return false;
            } catch (IOException e) {
                Log.e("EinzServer/launch", "IOException while calling serverSocket.accept().");
                e.printStackTrace();
                return false;
            }

            new Thread(new EinzServerThread(socket)).start(); // start new thread for this client.

        }
        return true;
    }

    public boolean isShouldStopServer() {
        return shouldStopServer;
    }

    /**
     * @param shouldStopServer true if the server should stop waiting for incoming connections
     */
    public void stopServer(boolean shouldStopServer) {
        this.shouldStopServer = shouldStopServer;

        if(shouldStopServer){
            // interrupt the serverSocket.accept() call, so that it will throw a SocketException
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("EinzServer/stopServer", "Tried to close socket. Failed.");
                e.printStackTrace();
            }
        }
    }
}
