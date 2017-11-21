package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import static ch.ethz.inf.vs.a4.minker.einz.BuildConfig.DEBUG;

/**
 * Stores Configuration of {@link ThreadedEinzServer} that is not suited for the communications-only part, because it has to do with the content of the messages,<br>
 * But is also not really relevant to the serverlogic<br>
 * This also registers and handles mappings that serverlogic does not handle. Initial Mappings are loaded from resources<br>
 * This class contains broadcastToSpectators while the ThreadedEinzServer contains sendMessageToUsername. That is basically the
 * difference between what belongs into this class and what into the server itself. Abstraction of game messaging vs pure messaging
 */
public class EinzServerManager {

    private final ThreadedEinzServer server;
    private ServerFunctionDefinition serverFunctionInterface;

    // These files are used to initialize the ActionFactories and ParserFactories of the EinzServerClientHandler threads
    // TODO: test the initialization of the factories once we have enough messages
    private final int networkingParserFile = R.raw.initialnetworkingparsermappings; // the networking parser shall be loaded from here
    private final int gameLogicParserFile = R.raw.initialgamelogicparsermappings; // the gamelogic parser shall be loaded from here
    private final int networkingActionFile = R.raw.initialnetworkingactionmappings; // the mapping from messagebodytype to action
    private final int gameLogicActionFile = R.raw.initialgamelogicactionmappings; // same, but for game logic instead

    private ConcurrentHashMap<String, EinzServerClientHandler> registeredClientHandlers; // list of only the registered clients, accessible by username
    // used to keep track of currently registered usernames
    private ConcurrentHashMap<String, String> registeredClientRoles; // mapping client usernames to roles

    public EinzServerManager(ThreadedEinzServer whotomanage, ServerFunctionDefinition serverFunctionInterface){
        this.server = whotomanage;
        this.registeredClientHandlers = new ConcurrentHashMap<>();
        this.registeredClientRoles = new ConcurrentHashMap<>();
        this.serverFunctionInterface = serverFunctionInterface;
    }
    protected String adminUsername; // TODO: store admin

    /**
     * @return List of registered users.
     */
    protected ArrayList<String> getConnectedUsers(){ //TODO: remove users on disconnect
        return new ArrayList<>(this.registeredClientHandlers.keySet());
    }

    public void finishRegistrationPhaseAndStartGame(){ // TODO: disable writing to registeredClientHandlers if game started. Make everything synchronized
        Log.d("Manager", "finishing registrationphase.");
        server.stopListeningForIncomingConnections(true);
        ArrayList<Player> players = new ArrayList<>();
        ArrayList<Spectator> spectators = new ArrayList<>();
        for(Map.Entry<String, EinzServerClientHandler> entry : registeredClientHandlers.entrySet()){
            EinzServerClientHandler handler = (EinzServerClientHandler) entry.getValue();
            String role = getRegisteredClientRoles().get(entry.getKey());
            if ( role.toLowerCase().equals("player")) {
                players.add(new Player((String) entry.getKey()));
            } else if ( role.toLowerCase().equals("spectator")){
                spectators.add(new Spectator((String) entry.getKey()));
            }
            // TODO: send that info to clients
        }
        Log.d("Manager/finishRegPhase", "Players: "+players.toString());
        GameState gameState = getServerFunctionInterface().startStandartGame(players); // returns gamestate but also modifies it internally, so i can discard the return value if I want to

    }

    public void loadAndRegisterNetworkingActions(EinzActionFactory actionFactory) throws JSONException, InvalidResourceFormatException, ClassNotFoundException { //TODO: register networking actions, maybe from json file
        InputStream jsonStream = server.applicationContext.getResources().openRawResource(this.networkingActionFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        actionFactory.loadMappingsFromJson(jsonObject);
    }

    public void loadAndRegisterGameLogicActions(EinzActionFactory actionFactory) throws InvalidResourceFormatException, JSONException, ClassNotFoundException { //TODO: register actions for game logic. from different json file
        InputStream jsonStream = server.applicationContext.getResources().openRawResource(this.gameLogicActionFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        actionFactory.loadMappingsFromJson(jsonObject);
    }


    /**
     * Load the resource file containing an Array of Pair<String messagegroup, Class<\? extends EinzParser>
     *     The rawResourceFile is set in {@link EinzServerManager#networkingParserFile} e.g. R.raw.serverDefaultParserMappings
     *                            This file should be formatted as a JSONObject containing a JSONArray "parsermappings" of JSONObjects of the form
     *                            {"messagegroup":"some thing", "mapstoparser":{...}}
     *                            where {...} stands for the JSON representation of the EinzParser class, e.g. <i>class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser</i>
     *     @throws JSONException if some of the JSON is not as expected
     *     @throws InvalidResourceFormatException if the mapping objects themselves are not valid. Contains more details in extended message
     */
    public void loadAndRegisterNetworkingParsers(EinzParserFactory parserFactory) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        InputStream jsonStream = server.applicationContext.getResources().openRawResource(this.networkingParserFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        parserFactory.loadMappingsFromJson(jsonObject);
    }

    public void loadAndRegisterGameLogicParsers(EinzParserFactory parserFactory) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        InputStream jsonStream = server.applicationContext.getResources().openRawResource(this.gameLogicParserFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        parserFactory.loadMappingsFromJson(jsonObject);
    }

    // https://stackoverflow.com/questions/6774579/typearray-in-android-how-to-store-custom-objects-in-xml-and-retrieve-them
    // utility function
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public ServerFunctionDefinition getServerFunctionInterface() {
        return serverFunctionInterface;
    }

    public void setServerFunctionInterface(ServerFunctionDefinition serverFunctionInterface) {
        this.serverFunctionInterface = serverFunctionInterface;
    }

    private boolean isInvalidUsername(String username){
        return (username.equals("") || username.equals("server"));
    }

    private boolean isLobbyFull(){
        return false;
    }

    /**
     * Threadsafe because of ConcurrentHashMap
     * Returns (as a freebie) an EinzMessage which could be used as reponse to the client. This Message is either of Type RegisterSuccess or RegisterFailure
     * Not yet fully implemented.
     * @param username
     * @param handler
     */
    public EinzMessage registerUser(String username, String role, EinzServerClientHandler handler){

        String reason = "unknown";
        boolean success = false;
        filterFailureReasons:
        {
            String connectedUser = handler.getConnectedUser();
            if(connectedUser!=null && !connectedUser.equals(username)){
                reason = "already registered";
                break filterFailureReasons; // don't even try registering, this client handler already is registered
            }

            if(isInvalidUsername(username)){
                reason = "invalid";
                break filterFailureReasons;
            }

            if(isLobbyFull()){
                reason = "lobby full";
                break filterFailureReasons;
            }

            EinzServerClientHandler res = getRegisteredClientHandlers().putIfAbsent(username, handler);
            // res is null if it was not set before this call, else it is the previous value

            if(res != null && res.equals(handler)){
                reason = "already registered";
                break filterFailureReasons;
            }

            success = (res == null); // success only if nobody was registered (for this username)
            Log.d("serverManager/reg", "registered " + username + ". Success: " + success);



            // set admin to this user if he was the first connection and registered successfully
            if (success && handler.isFirstConnectionOnServer()) adminUsername = username;

            if (success) {
                String absent = getRegisteredClientRoles().putIfAbsent(username, role); //it really should be absent
                if (DEBUG && absent != null)
                    throw new RuntimeException(new java.lang.Exception("serverManager/reg: username was absent in registeredClientHandlers but not in registeredClientRoles!"));
                handler.setConnectedUser(username); // tell the handler which user it is connected to
            }
        }

        EinzMessage response = null;

        if(success){
            EinzMessageHeader header = new EinzMessageHeader("registration", "RegisterSuccess");
            EinzRegisterSuccessMessageBody body = new EinzRegisterSuccessMessageBody(username, role);
            response = new EinzMessage<EinzRegisterSuccessMessageBody>(header, body);
            handler.setConnectedUser(username);
        } else {
            EinzMessageHeader header = new EinzMessageHeader("registration", "RegisterFailure");
            EinzRegisterFailureMessageBody body = new EinzRegisterFailureMessageBody(role, username, reason);
            response = new EinzMessage<EinzRegisterFailureMessageBody>(header, body);
        }
        return response;
    }

    /**
     * Unregisters user and generates message to be broadcasted to inform clients that this user left. <br>
     *     <b>Broadcasts the message already!</b>
     *     Returns the response (different from the broadcast!)
     *     Make sure to check whether the user is allowed to kick somebody if you call this to kick. Also make sure to check that this user exists.
     * @param username who to remove
     *
     * @return The message only for the client who issued the unregister/kick request. Ignore this return in case of a normal unregister<br>
     *     <i>null</i> if there was no failure
     */
    public EinzMessage<EinzKickFailureMessageBody> unregisterUser(String username, String unregisterReason){
        String failureReason = null;
        EinzMessage<EinzUnregisterResponseMessageBody> einzMessage;

        if(username==null || isInvalidUsername(username)){
            failureReason = "invalid";
        }

        if(failureReason==null){
            ConcurrentHashMap<String, String> clientRoles = getRegisteredClientRoles();
            ConcurrentHashMap<String, EinzServerClientHandler> clientHandlers = getRegisteredClientHandlers();
                    // TODO: does getRegisteredClientRoles and getRegisteredClientHandlers introduce bugs because the state might change between accessing the two?
            String role = clientRoles.get(username);
            EinzServerClientHandler esch = clientHandlers.get(username);
            // unregister them
            getRegisteredClientRoles().remove(username);
            getRegisteredClientHandlers().remove(username);

            // inform all clients
            EinzUnregisterResponseMessageBody body = new EinzUnregisterResponseMessageBody(username, unregisterReason);
            EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterResponse");
            EinzMessage<EinzUnregisterResponseMessageBody> message = new EinzMessage<>(header, body);
            broadcastMessageToAllPlayers(message);
            broadcastMessageToAllSpectators(message);

            // and stop the corresponding client
            esch.setConnectedUser(null);
            esch.stopThreadPatiently();

        }

        EinzMessage<EinzKickFailureMessageBody> emz;
        EinzMessageHeader header = new EinzMessageHeader("registration", "KickFailure");
        // generate response
        if(failureReason==null){
            return null;
        } else{
            EinzKickFailureMessageBody body = new EinzKickFailureMessageBody(username, failureReason);
            emz = new EinzMessage<>(
                header, body
            );
            return emz;
        }
    }

    // TODO: kickUser -- including checking if they are allowed to kick and if the user exists.

    public EinzMessage<EinzUpdateLobbyListMessageBody> generateUpdateLobbyListRequest(){
        EinzMessageHeader header = new EinzMessageHeader("registration", "UpdateLobbyList");
        /*
                {
          "header":{
            "messagegroup":"registration",
            "messagetype":"UpdateLobbyList"
          },
          "body":{
            "lobbylist":[
              {"roger":"player"},
              {"chris":"player"},
              {"table":"spectator"}
            ],
            "admin":"roger"
          }
        }
         */
        ConcurrentHashMap<String, String> chm = getRegisteredClientRoles();
        @SuppressWarnings("unchecked")
        HashMap<String, String> localCopy = (HashMap<String, String>) new HashMap(chm);
        EinzUpdateLobbyListMessageBody body = new EinzUpdateLobbyListMessageBody(localCopy, getAdminUsername());

        return new EinzMessage<>(header, body);
    }

    public ConcurrentHashMap<String, String> getRegisteredClientRoles() {
        return registeredClientRoles;
    }

    public ConcurrentHashMap<String, EinzServerClientHandler> getRegisteredClientHandlers() {
        return registeredClientHandlers;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void broadcastMessageToAllPlayers(EinzMessage message) {
        Log.d("servMan/broadcastP", "broadcasting "+message.getBody().getClass().getSimpleName());
        ConcurrentHashMap<String, EinzServerClientHandler> map = getRegisteredClientHandlers();
        for(String username : getRegisteredClientRoles().keySet()){
            if(!getRegisteredClientRoles().get(username).toLowerCase().equals("player"))
                continue;

            try {
                this.server.sendMessageToUser(username, message);
            } catch (UserNotRegisteredException e) {
                // this shouldn't happen
                Log.e("servMan/broadcastP", "This shouldn't happen: failed to send message because user is unregistered.");
                throw new RuntimeException(e);
            } catch (JSONException e) {
                Log.e("servMan/broadcastP", "Exception while translating message");
                throw new RuntimeException(e);
            }
        }
    }

    public void broadcastMessageToAllSpectators(EinzMessage message) {
        for(String username : getRegisteredClientRoles().keySet()){
            if(!getRegisteredClientRoles().get(username).toLowerCase().equals("spectator"))
                continue;

            try {
                this.server.sendMessageToUser(username, message);
            } catch (UserNotRegisteredException e) {
                // this shouldn't happen
                Log.e("servMan/broadcastS", "This shouldn't happen: failed to send message because user is unregistered.");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.e("servMan/broadcastS", "Exception while translating message");
                throw new RuntimeException(e);
            }
        }
    }
}
