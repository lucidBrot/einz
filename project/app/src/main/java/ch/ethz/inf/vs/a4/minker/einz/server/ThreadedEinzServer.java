package ch.ethz.inf.vs.a4.minker.einz.server;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import ch.ethz.inf.vs.a4.minker.einz.GameState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private BiMap<Thread, EinzServerClientHandler> clientHandlerBiMap = HashBiMap.create(); // list of registered clients and ESCHs

    private int numClients;
    private ServerActivityCallbackInterface serverActivityCallbackInterface;
    final Context applicationContext;
    private final EinzServerManager serverManager;
    private ReentrantReadWriteLock sherLock = new ReentrantReadWriteLock(); // locks everything here (not servermanager) i.e. glienthandlerhtreads, numClients and clienthandlerthreads

    /**
     * @param PORT specifies the port to use. If the port is already in use, we will still use a different port
     */
    public ThreadedEinzServer(Context applicationContext, int PORT, ServerActivityCallbackInterface serverActivityCallbackInterface, ServerFunctionDefinition serverFunctionDefinition){
        this.PORT = PORT;
        this.serverActivityCallbackInterface = serverActivityCallbackInterface;
        this.serverManager = new EinzServerManager(this, serverFunctionDefinition);
        this.applicationContext = applicationContext;
    }

    /**
     * Listen on any one free port. Dispatch an EinzServerThread for every Connection
     */
    public ThreadedEinzServer(Context applicationContext, ServerActivityCallbackInterface serverActivityCallbackInterface, ServerFunctionDefinition serverFunctionDefinition){
        this(applicationContext,0, serverActivityCallbackInterface, serverFunctionDefinition);
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
        Log.d("EinzServer/launch","\nlaunching Server on "+PORT);
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
            Log.d("EinzServer", "DEBUG_ONE_MSG: Will simulate messages from a client");
            DEBUG_ONE_MSG = false;
            Debug.debug_simulateClient1();
            Debug.debug_simulateClient2();
        }

        boolean firstconnection = true;
        while (!shouldStopSpinning){

            try {
                socket = serverSocket.accept();
                Log.d("EinzServer/launch", "new connection from "+socket.getInetAddress()+":"+socket.getPort());
            } catch (SocketException e){
                if(shouldStopSpinning) {
                    Log.d("EinzServer/launch", "stopping accepting connections");
                    //DEBUG to test if connection is not accepting anymore closed
                    //Debug.debug_simulateClient2();
                }
                else
                    Log.d("EinzServer/launch","SocketException but shouldStopSpinning is false");
                return false;
            } catch (IOException e) {
                // connection seems to have done weird things. Nevermind, just try again after logging
                Log.e("EinzServer/launch", "IOException while calling serverSocket.accept().");
                e.printStackTrace();
                return false;
            }
            this.sherLock.writeLock().lock();// for firstconnection, so that it doesn't change and we get two admins
            getServerManager().getSFLock().readLock().lock();
            EinzServerClientHandler ez = new EinzServerClientHandler(socket, this, this.getServerManager().getServerFunctionInterface(),firstconnection);
            getServerManager().getSFLock().readLock().unlock();
            Thread thread = new Thread(ez);
            clientHandlerBiMap.put(thread, ez);

            firstconnection = false; // the first user has connected, all others cannot be admin
            this.sherLock.writeLock().unlock();
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
    public void stopListeningForIncomingConnections(boolean shouldStopSpinning) {
        this.shouldStopSpinning = shouldStopSpinning;

        if(shouldStopSpinning){
            // interrupt the serverSocket.accept() call, so that it will throw a SocketException
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("EinzServer/stopServer", "Tried to close socket. Failed.");
                // probably because it lost connection but still has buffer to flush. Don't care, just finish that
                e.printStackTrace();
            }
        }
    }

    /**
     * Kills all clientHandler threads. Use this in case of an emergency, because Clients will not be informed.
     * THIS WILL PUT THE SERVER INTO UNDEFINED STATE. ONLY USE IT AS A KILL SWITCH
     */
    public void abortAllClientHandlers(){
        this.sherLock.writeLock().lock();
            for (Thread ez : clientHandlerBiMap.keySet()) {
                if (ez.getState() != Thread.State.TERMINATED)
                    ez.stop();
            }
        this.sherLock.writeLock().unlock();
    }

    public int getPORT() {
        return PORT;
    }

    public int getNumClients() {
        return numClients;
    }

    public void incNumClients(){
        this.sherLock.writeLock().lock();
        this.numClients++;
        int temp = this.numClients;
        serverActivityCallbackInterface.updateNumClientsUI(temp);
        this.sherLock.writeLock().unlock();
    }

    public void decNumClients(){
        this.sherLock.writeLock().lock();
        this.numClients--;
        serverActivityCallbackInterface.updateNumClientsUI(numClients);
        this.sherLock.writeLock().unlock();
    }

    /**
     * sends a (usually JSON-encoded, one-line) message to user specified by username. If the message does not end with "\r\n", that will be appended.
     * <br>Threadsafe ✔ Writelock on socket<br>
     * @param username target registered user as String
     * @param message JSON-encoded message as String
     * @throws UserNotRegisteredException if username is not registered
     */
    public void sendMessageToUser(String username, String message) throws UserNotRegisteredException {
        getServerManager().getUserListLock().readLock().lock();
        EinzServerClientHandler ez = getServerManager().getRegisteredClientHandlers().get(username);
        getServerManager().getUserListLock().readLock().unlock();
        int lindex = message.lastIndexOf("\n");
        if(message.substring(0,(lindex>=0)?lindex:0).contains("\n")){
            Log.w("EinzServer/sendMsg", "the message contains multiple newlines: "+message);
        }
        if(!message.endsWith("\r\n")){
            message += "\r\n";
        }
        if (ez == null) {
            throw new UserNotRegisteredException("Cannot send message to not registered username");
        }else{
            ((EinzServerClientHandler) ez).sendMessage(message);
        }

    }

    /**
     * Calls this.sendMessageToUser, but with the message transformed to a JSON string
     * <br>Threadsafe ✔ Writelock on socket<br>
     * @param username to whom to send this message
     * @param message what to send
     * @throws UserNotRegisteredException if the user is not found
     * @throws JSONException if the message.toJSON() failed
     */
    public void sendMessageToUser(String username, EinzMessage message) throws UserNotRegisteredException, JSONException {
        this.sendMessageToUser(username, message.toJSON().toString());
    }

    public EinzServerManager getServerManager() {
        return serverManager;
    }

    /**
     * Waits for all clientHanlderThreads after kicking all clients. may take some time. You should consider running this method in a non-UI thread.
     */
    public void shutdown() {
        Log.d("EinzServer/shutdown", "initiating shutdown...");
        stopListeningForIncomingConnections(true);
        this.sherLock.writeLock().lock();
        getServerManager().kickAllAndCloseSockets();
        // waiting because clientHandlerThreads might still need this server
        for(Thread t : this.clientHandlerBiMap.keySet()){
            try {
                t.join();
            } catch (InterruptedException e) {
                Log.w("EinzServer/shutdown", "couldn't wait for thread.");
                e.printStackTrace();
            }
        }
        this.sherLock.writeLock().unlock();
        Log.d("EinzServer/shutdown", "finished shutting down server");
    }

    public void removeEinzServerClientHandlerFromClientHandlerList(EinzServerClientHandler handler) {
        this.sherLock.writeLock().lock();
        this.clientHandlerBiMap.inverse().remove(handler);
        this.sherLock.writeLock().unlock();
    }
}
