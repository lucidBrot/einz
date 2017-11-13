package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import android.util.Pair;
import ch.ethz.inf.vs.a4.minker.einz.GameState;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Waits for incoming TCP connections, handles each in a separate Thread, dispatches an EinzServerThread for every client
 * This class spins. It is a Runnable, supposed to be run in its own thread.
 * Call stopSpinning() to stop accepting new connections
 */
public class ThreadedEinzServer implements Runnable { // apparently, 'implements Runnable' is better than 'extends thread': https://stackoverflow.com/questions/5853167/runnable-with-a-parameter
    private int PORT;
    private boolean shouldStopSpinning = false;
    private ServerSocket serverSocket;
    private boolean DEBUG_ONE_MSG = true; // if true, this will simulate sending a debug message from the client
    private ArrayList<Thread> clientHandlerThreads; // list of registered clients. use .getState to check if it is still running
    private HashMap<String, Pair<EinzServerClientHandler, Thread>> registeredClientHandlers; // list of only the registered clients, accessible by username
    public GameState gameState;
    private int numClients;
    private ServerActivityCallbackInterface serverActivityCallbackInterface;
    private ServerFunctionDefinition serverFunctionDefinition; // interface to server logic

    /**
     * @param PORT specifies the port to use. If the port is already in use, we will still use a different port
     */
    public ThreadedEinzServer(int PORT, ServerActivityCallbackInterface serverActivityCallbackInterface, ServerFunctionDefinition serverFunctionDefinition){
        this.PORT = PORT;
        clientHandlerThreads = new ArrayList<Thread>();
        this.serverActivityCallbackInterface = serverActivityCallbackInterface;
        this.serverFunctionDefinition = serverFunctionDefinition;
    }

    /**
     * Listen on any one free port. Dispatch an EinzServerThread for every Connection
     */
    public ThreadedEinzServer(ServerActivityCallbackInterface serverActivityCallbackInterface, ServerFunctionDefinition serverFunctionDefinition){
        this(0, serverActivityCallbackInterface, serverFunctionDefinition);
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
        Log.d("EinzServer/launch","launching Server on "+PORT);
        serverSocket = null;
        Socket socket = null;

        // code example: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server

        try {
            serverSocket = new ServerSocket(PORT);
            PORT = serverSocket.getLocalPort();
            Log.d("EinzServer/launch", "updated PORT to "+PORT);
        } catch (IOException e) {
            Log.e("EinzServer/launch", "IOException while creating serverSocket on Port "+PORT);
            Log.e("EinzServer/launch", "Error Message: "+e.getMessage());
            e.printStackTrace();
            Log.e("EinzServer/launch", "Retrying with a different port");
            try {
                serverSocket = new ServerSocket(0); // chooses a free port if available
                PORT = serverSocket.getLocalPort();
            } catch (IOException e1) {
                Log.e("EinzServer/launch", "Retrying didn't work - possibly no free ports available?");
                Log.e("EinzServer/launch", "Error Message: "+e1.getMessage());
                e1.printStackTrace();
            }
            return false;
        }

        //DEBUG: Simulate a message from a client
        if(DEBUG_ONE_MSG){
            Log.d("ThreadedEinzServer", "DEBUG_ONE_MSG: Will simulate a message from a client");
            DEBUG_ONE_MSG = false;
            //<DEBUG>
            final TempClient tc = new TempClient(new TempClient.OnMessageReceived() {
                @Override
                public void messageReceived(String message) {
                    Log.d("TempClient", "received message "+message);
                }
            });
            Thread t = new Thread(){
                @Override
                public void run() {
                    // DEBUG: start client
                    // temporary. please do not use in real code
                    Log.d("EinzServer/debug", "simulating client");

                    tc.run();
                }
            };
            t.start(); // start client stub for debug
            Thread m = new Thread(){
                @Override
                public void run() {
                    Log.d("TempClient", "calling sendMessage");
                    try {
                        sleep(600); // wait until server hopefully runs
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e("TempClient", "Sleeping Failed");
                        interrupt();
                    }
                    JSONObject myJSONObject = new JSONObject();
                    String message = "test message";
                    try {
                        myJSONObject.put("messagetype", "debug message");
                        myJSONObject.accumulate("val", 1);
                        myJSONObject.accumulate("val", 2);
                        message = myJSONObject.toString();
                        Log.d("TempClient", "set message to JSON: "+message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    tc.sendMessage(message);
                }
            };
            m.start(); // send message
            //</Debug>
        }

        while (!shouldStopSpinning){

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

            EinzServerClientHandler ez = new EinzServerClientHandler(socket, this, this.serverFunctionDefinition);
            Thread thread = new Thread(ez);
            clientHandlerThreads.add(thread);
            thread.start(); // start new thread for this client.

        }
        return true;
    }

    public boolean isShouldStopSpinning() {
        return shouldStopSpinning;
    }

    /**
     * @param shouldStopSpinning true if the server should stop waiting for incoming connections
     */
    public void stopSpinning(boolean shouldStopSpinning) {
        this.shouldStopSpinning = shouldStopSpinning;

        if(shouldStopSpinning){
            // interrupt the serverSocket.accept() call, so that it will throw a SocketException
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("EinzServer/stopServer", "Tried to close socket. Failed.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Kills all clientHandler threads. Use this in case of an emergency, because Clients will not be informed.
     * THIS WILL PUT THE SERVER INTO UNDEFINED STATE. ONLY USE IT AS A KILL SWITCH
     */
    public void abortAllClientHandlers(){
        for (Thread ez : clientHandlerThreads){
            if(ez.getState() != Thread.State.TERMINATED)
                ez.stop();
        }
    }

    public int getPORT() {
        return PORT;
    }

    public int getNumClients() {
        return numClients;
    }

    public void incNumClients(){
        this.numClients++;
        serverActivityCallbackInterface.updateNumClientsUI(numClients);
    }

    public void decNumClients(){
        this.numClients--;
        serverActivityCallbackInterface.updateNumClientsUI(numClients);
    }

    /**
     * sends a (usually JSON-encoded, one-line) message to user. If the message does not end with "\r\n", that will be appended.
     * @param username target registered user as String
     * @param message JSON-encoded message as String
     * @throws UserNotRegisteredException if username is not registered
     */
    public void sendMessageToUser(String username, String message) throws UserNotRegisteredException {
        Pair pair = registeredClientHandlers.get(username);
        if(!message.endsWith("\r\n")){ // TODO: check if contains newline and if yes, abort
            message += "\r\n";
        }
        if (pair == null) {
            throw new UserNotRegisteredException("Cannot send message to not registered username");
        }else{
            ((EinzServerClientHandler) pair.first).sendMessage(message);
        }

    }

    /**
     * To be called by EinzServerClientHandler when user registers.
     * If the entry for username already exists, it will be replaced
     * @param username key
     * @param einzServerClientHandler value: the client handler
     * @param thread value: The thread which contains the einzServerClientHandler
     */
    public void registerUser(String username, EinzServerClientHandler einzServerClientHandler, Thread thread){
        registeredClientHandlers.put(username, Pair.create(einzServerClientHandler,thread));
    }

    /**
     * To be called by EinzServerClientHandler when user deregisters
     * @param username
     */
    public void deregisterUser(String username){
        registeredClientHandlers.remove(username);
    }
}
