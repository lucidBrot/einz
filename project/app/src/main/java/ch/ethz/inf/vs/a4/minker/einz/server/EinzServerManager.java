package ch.ethz.inf.vs.a4.minker.einz.server;

import android.support.annotation.Nullable;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    private boolean gamePhaseStarted;

    // These files are used to initialize the ActionFactories and ParserFactories of the EinzServerClientHandler threads
    private final int networkingParserFile = R.raw.initial_networking_parser_mappings; // the networking parser shall be loaded from here
    private final int gameLogicParserFile = R.raw.initial_game_logic_parser_mappings; // the gamelogic parser shall be loaded from here
    private final int networkingActionFile = R.raw.server_initial_networking_action_mappings; // the mapping from messagebodytype to action
    private final int gameLogicActionFile = R.raw.server_initial_game_logic_action_mappings; // same, but for game logic instead

    private ConcurrentHashMap<String, EinzServerClientHandler> registeredClientHandlers; // list of only the registered clients, accessible by username
    // used to keep track of currently registered usernames
    private ConcurrentHashMap<String, String> registeredClientRoles; // mapping client usernames to roles
    protected String adminUsername;

    public ReentrantReadWriteLock getUserListLock() { // registeredClientRoles, registeredClientHandlers, gamePhaseStarted
        return userListLock;
    }

    private ReentrantReadWriteLock userListLock = new ReentrantReadWriteLock(); // used for accessing any of the above to ensure consistency within them
        //Do not write to any of the above without locking. Do not read without read-locking if you're worried about inconsistencies

    public EinzServerManager(ThreadedEinzServer whotomanage, ServerFunctionDefinition serverFunctionInterface){
        this.server = whotomanage;
        this.registeredClientHandlers = new ConcurrentHashMap<>();
        this.registeredClientRoles = new ConcurrentHashMap<>();
        this.serverFunctionInterface = serverFunctionInterface;
        this.gamePhaseStarted = false;
    }

    /**
     * @return List of registered users.
     */
    protected ArrayList<String> getConnectedUsers(){
        return new ArrayList<>(this.registeredClientHandlers.keySet());
    }


    public void finishRegistrationPhaseAndStartGame(){
        Log.d("servMan", "finishing registrationphase.");
        userListLock.readLock().lock();
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
            // TODO: send that info to clients. Fabian?
        }
        Log.d("servMan/finishRegPhase", "Players: "+players.toString());
        userListLock.readLock().unlock();
        userListLock.writeLock().lock();
        this.gamePhaseStarted = true;
        userListLock.writeLock().unlock();
        GameState gameState = getServerFunctionInterface().initialiseStandardGame(players, spectators); // returns gamestate but also modifies it internally, so i can discard the return value if I want to

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

            getUserListLock().writeLock().lock();
            if(gamePhaseStarted){
                reason = "game already in progress";
                break filterFailureReasons;
            }

            EinzServerClientHandler res = getRegisteredClientHandlers().putIfAbsent(username, handler);
            // res is null if it was not set before this call, else it is the previous value

            if(res != null && res.equals(handler)){
                reason = "already registered";
                break filterFailureReasons;
            }

            if(res != null && !res.equals(handler)){
                reason = "not unique";
                break filterFailureReasons;
            }

            success = (res == null); // success only if nobody was registered (for this username)
            Log.d("serverManager/reg", "registered " + username + ". Success: " + success);



            // set admin to this user if he was the first connection and registered successfully
            if (success && handler.isFirstConnectionOnServer()) adminUsername = username;

            if (success) {
                handler.setConnectedUser(username); // tell the handler which user it is connected to
                String absent = getRegisteredClientRoles().putIfAbsent(username, role); //it really should be absent
                userListLock.writeLock().unlock();
                if (DEBUG && absent != null) { // assertion on android
                    userListLock.writeLock().unlock();
                    throw new RuntimeException(new java.lang.Exception("serverManager/reg: username was absent in registeredClientHandlers but not in registeredClientRoles!"));
                }

            }
        }

        if(userListLock.writeLock().isHeldByCurrentThread())
            userListLock.writeLock().unlock();

        EinzMessage response = null;

        if(success){
            EinzMessageHeader header = new EinzMessageHeader("registration", "RegisterSuccess");
            EinzRegisterSuccessMessageBody body = new EinzRegisterSuccessMessageBody(username, role);
            response = new EinzMessage<EinzRegisterSuccessMessageBody>(header, body);
        } else {
            EinzMessageHeader header = new EinzMessageHeader("registration", "RegisterFailure");
            EinzRegisterFailureMessageBody body = new EinzRegisterFailureMessageBody(role, username, reason);
            response = new EinzMessage<EinzRegisterFailureMessageBody>(header, body);
        }
        Log.d("serverMan/reg", username+" came this far [end of ServerMan/registerUser()] (admin="+adminUsername+").");
        return response;
    }

    /**
     * Unregisters user and generates message to be broadcasted to inform clients that this user left. ({@link EinzUnregisterResponseMessageBody} and {@link EinzUpdateLobbyListMessageBody} <br>
     *     <b>Broadcasts the message already!</b>
     *     Returns the response if kicking failed (different from the broadcast!) or null. This includes null if this was not a kick.
     *     Make sure to check whether the user is allowed to kick somebody if you call this to kick. Also make sure to check that this user exists.
     *     <br>This function responds to the client who requested this. the return message is only to check for what happened.
     * @param username who to remove
     * @param issuedByUser who wanted this kick/unregister. Can be the same as username
     * @param unregisterReason "kicked", "timeout", or "disconnect"(voluntary)
     *
     * @return The message only for the client who issued the unregister/kick request. Ignore this return in case of a normal unregister<br>
     *     <i>null</i> if there was no failure
     */
    @Nullable
    public EinzMessage<EinzKickFailureMessageBody> unregisterUser(String username, String unregisterReason, String issuedByUser){
        getUserListLock().readLock().lock();
        String failureReason = null;
        EinzMessage<EinzKickFailureMessageBody> returnMessage;

        if(username==null || ( isInvalidUsername(username) && !username.equals("server"))){
            failureReason = "invalid";
        }


        //       if kicked check for failure and respond with it, then broadcast unregisterresponse
        //       if timout, do the same as if unregistered by disconnect
        //       if disconnected, only broadcast unregisterresponse, with no response to the player who disconnected
        if(unregisterReason.toLowerCase().equals("kicked")){
            if(failureReason!=null){
                Log.d("servMan/unregUser", "Sending KickFailure Message (issued by \"+issuedByUser+\") for kicking "+username );
                EinzKickFailureMessageBody kickFailureMessageBody = new EinzKickFailureMessageBody(username, failureReason);
                EinzMessageHeader header = new EinzMessageHeader("registration", "KickFailure");
                EinzMessage<EinzKickFailureMessageBody> kickFailureMessage = new EinzMessage<>(header, kickFailureMessageBody);
                returnMessage = kickFailureMessage;
                try {
                    this.getServer().sendMessageToUser(issuedByUser, kickFailureMessage);
                } catch (UserNotRegisteredException e) {
                    Log.w("servMan/unregUser", "The user who requested a kick does no longer exist!");
                    // continue because hopefully the kick itself was valid but this user left in the meantime
                    e.printStackTrace();
                } catch (JSONException e) { // this is something that should be correct by design!
                    throw new RuntimeException(e);
                }
            } else {
                returnMessage = null; // null stands for successful kicking or that it wasn't a kick
                Log.d("servMan/unregUser", "kicking "+username+"...");
                if(gamePhaseStarted){
                    // TODO: removeUser from fabian
                    serverFunctionInterface.removePlayer(username);
                }
            }
        }
        getUserListLock().readLock().unlock();

        if(failureReason==null){
            userListLock.readLock().lock();
            ConcurrentHashMap<String, String> clientRoles = getRegisteredClientRoles();
            ConcurrentHashMap<String, EinzServerClientHandler> clientHandlers = getRegisteredClientHandlers();

            String role = clientRoles.get(username);
            EinzServerClientHandler esch = clientHandlers.get(username);
            userListLock.readLock().unlock();

            // inform all clients
            // broadcast UnregisterResponse
            EinzUnregisterResponseMessageBody body = new EinzUnregisterResponseMessageBody(username, unregisterReason);
            EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterResponse");
            EinzMessage<EinzUnregisterResponseMessageBody> message = new EinzMessage<>(header, body);
            broadcastMessageToAllPlayers(message);
            broadcastMessageToAllSpectators(message);

            //broadcast updateLobbyList
            EinzMessage<EinzUpdateLobbyListMessageBody> msg = generateUpdateLobbyListRequest();
            broadcastMessageToAllPlayers(msg);
            broadcastMessageToAllSpectators(msg);

            // unregister them
            userListLock.writeLock().lock();
            getRegisteredClientRoles().remove(username);
            getRegisteredClientHandlers().remove(username);
            userListLock.writeLock().unlock();

            // and stop the corresponding client
            esch.setConnectedUser(null);
            esch.stopThreadPatiently();
            Log.d("servMan/unreg", "unregistered user "+username);

        } else {
            Log.d("servMan/unreg", "failed to unregister user "+username+" because "+failureReason);
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

    /**
     * Tests if user is allowed to perform this action and performs it.
     * Says user is not allowed if it was not yet registered as admin. That shouldn't happen though, as messages are supposed to be ordered by client so there should always be a register first.
     * @param userToKick
     * @param userWhoIssuedThisKick
     */
    public void kickUser(String userToKick, String userWhoIssuedThisKick){
        userListLock.readLock().lock();
        EinzServerClientHandler esch = getRegisteredClientHandlers().get(userToKick);
        // if admin is not yet set, don't kick
        boolean allowed = ((getAdminUsername()!=null && getAdminUsername().equals(userWhoIssuedThisKick))||userWhoIssuedThisKick.equals("server"));
        boolean userExists = (esch!=null);
        boolean userValid = !isInvalidUsername(userToKick);

        EinzMessage<EinzKickFailureMessageBody> response;
        if(userExists && allowed && userValid) {
            response = unregisterUser(userToKick, "kicked", userWhoIssuedThisKick);
            if(response==null)//if success
            {
                userListLock.readLock().unlock();
                return;
            } else{
                // failure. send to user.
                try {
                    server.sendMessageToUser(userWhoIssuedThisKick, response);
                } catch (UserNotRegisteredException e) {
                    Log.w("servMan/kick", "User "+userWhoIssuedThisKick+" was allowed to issue (kick "+userToKick+") but is not registered");
                    e.printStackTrace();
                    // User who issued this does not exist :(
                    // Guess that means we don't answer them
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                userListLock.readLock().unlock();
                return;
            }
        } else {
            // user doesn't exist or issuer is not admin
            EinzKickFailureMessageBody ekfmb;
            if(!allowed){
                 ekfmb = new EinzKickFailureMessageBody(userToKick, "not allowed");
            } else if(!userValid){
                ekfmb = new EinzKickFailureMessageBody(userToKick, "invalid");
            } else{
                ekfmb = new EinzKickFailureMessageBody(userToKick, "not found");
            }
            EinzMessageHeader header = new EinzMessageHeader("registration", "KickFailure");
            EinzMessage<EinzKickFailureMessageBody> message = new EinzMessage<>(header, ekfmb);
            try {
                server.sendMessageToUser(userWhoIssuedThisKick, message);
            } catch (UserNotRegisteredException e) {
                e.printStackTrace();
                // don't send to user if user doesn't exist (anymore)
            } catch (JSONException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        userListLock.readLock().unlock();
        return;
    }

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
        // Locking on userlistLock is not neccessary here because we only access one of these and concurrentHashMap guarantees an ok state
        ConcurrentHashMap<String, String> chm = getRegisteredClientRoles();
        @SuppressWarnings("unchecked")
        HashMap<String, String> localCopy = (HashMap<String, String>) new HashMap(chm);
        EinzUpdateLobbyListMessageBody body = new EinzUpdateLobbyListMessageBody(localCopy, getAdminUsername());

        return new EinzMessage<>(header, body);
    }

    /**
     * Locks onto {@link #userListLock} to ensure consistency
     * Make sure to lock for writing on this
     * @return
     */
    public ConcurrentHashMap<String, String> getRegisteredClientRoles() {
        this.userListLock.readLock().lock();
        ConcurrentHashMap<String, String> hm = registeredClientRoles;
        this.userListLock.readLock().unlock();
        return hm;
    }

    /**
     * Lock onto {@link #userListLock} to ensure consistency while reading this.
     * Make sure to lock for writing on this.
     * @return
     */
    public ConcurrentHashMap<String, EinzServerClientHandler> getRegisteredClientHandlers() {
        this.userListLock.readLock().lock();
        ConcurrentHashMap<String, EinzServerClientHandler> hm = registeredClientHandlers;
        this.userListLock.readLock().unlock();
        return hm;
    }

    /**
     * locks onto {@link #userListLock} to ensure consistency
     * @return
     */
    public String getAdminUsername() {
        return adminUsername;
    }

    public void broadcastMessageToAllPlayers(EinzMessage message) {
        Log.d("servMan/broadcastP", "broadcasting "+message.getBody().getClass().getSimpleName());
        userListLock.readLock().lock();
        for(String username : getRegisteredClientRoles().keySet()){

            if(!getRegisteredClientRoles().get(username).toLowerCase().equals("player"))
                continue;

            try {
                this.server.sendMessageToUser(username, message);
            } catch (UserNotRegisteredException e) {
                // this shouldn't happen
                userListLock.readLock().unlock();
                Log.e("servMan/broadcastP", "This shouldn't happen: failed to send message because user is unregistered.");
                throw new RuntimeException(e);
            } catch (JSONException e) {
                userListLock.readLock().unlock();
                Log.e("servMan/broadcastP", "Exception while translating message");
                throw new RuntimeException(e);
            }
        }
        userListLock.readLock().unlock();
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

    public boolean isGamePhaseStarted() {
        return gamePhaseStarted;
    }


    public ThreadedEinzServer getServer() {
        return server;
    }

    public boolean isRegistered(String username){
        return (username!=null && !isInvalidUsername(username) && getRegisteredClientRoles().contains(username));
    }

    public boolean isRegisteredAdmin(String username){
        return isRegistered(username) && getAdminUsername()!=null && getAdminUsername().equals(username);
    }

    public void startGame(String issuedByPlayer) {
        if(isRegisteredAdmin(issuedByPlayer)) {
            finishRegistrationPhaseAndStartGame(); //serverFunctionInterfae.initializeStandardGame is contained in this call
            serverFunctionInterface.startGame();
        }
        else{
            Log.e("servMan", "somebody unauthorized tried to start the game!");
        }
    }
}
