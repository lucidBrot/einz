package ch.ethz.inf.vs.a4.minker.einz.server;

import android.support.annotation.Nullable;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.*;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.model.Spectator;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.GlobalStateParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.PlayerState;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.*;
import ch.ethz.inf.vs.a4.minker.einz.rules.otherrules.CountNumberOfCardsAsPoints;
import org.json.JSONArray;
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
    private EinzSpecifyRulesMessageBody latestSpecifyRulesMessageBody;

    public ReentrantReadWriteLock getSFLock() {
        return SFLock;
    }

    private ReentrantReadWriteLock SFLock = new ReentrantReadWriteLock(); // lock when reading/writing to serverFunctionInterface by calling a function on there
    private boolean gamePhaseStarted;
    public boolean serverShuttingDownGracefully;

    // These files are used to initialize the ActionFactories and ParserFactories of the EinzServerClientHandler threads
    private final int networkingParserFile = R.raw.initial_networking_parser_mappings; // the networking parser shall be loaded from here
    private final int gameLogicParserFile = R.raw.initial_game_logic_parser_mappings; // the gamelogic parser shall be loaded from here
    private final int networkingActionFile = R.raw.server_initial_networking_action_mappings; // the mapping from messagebodytype to action
    private final int gameLogicActionFile = R.raw.server_initial_game_logic_action_mappings; // same, but for game logic instead

    private ConcurrentHashMap<String, EinzServerClientHandler> registeredClientHandlers; // list of only the registered clients, accessible by username
    // used to keep track of currently registered usernames
    private ConcurrentHashMap<String, String> registeredClientRoles; // mapping client usernames to roles
    protected String adminUsername;
    private ConcurrentHashMap<String,JSONObject> registeredClientPositioning; // used for finding out where a player is irl positioned

    public ReentrantReadWriteLock getUserListLock() { // registeredClientRoles, registeredClientHandlers, gamePhaseStarted
        return userListLock;
    }

    private ReentrantReadWriteLock userListLock = new ReentrantReadWriteLock(); // used for accessing any of the above to ensure consistency within them
        //Do not write to any of the above without locking. Do not read without read-locking if you're worried about inconsistencies

    public EinzServerManager(ThreadedEinzServer whotomanage, ServerFunctionDefinition serverFunctionInterface){
        this.server = whotomanage;
        this.registeredClientHandlers = new ConcurrentHashMap<>();
        this.registeredClientRoles = new ConcurrentHashMap<>();
        this.registeredClientPositioning = new ConcurrentHashMap<>();
        this.serverFunctionInterface = serverFunctionInterface;
        this.gamePhaseStarted = false;
    }

    /**
     * @return List of registered users.
     */
    protected ArrayList<String> getConnectedUsers(){
        return new ArrayList<>(this.registeredClientHandlers.keySet());
    }


    /**
     * stops the server from accepting new connections, calls initialize on the serverFunctionDefinition
     */
    public void finishRegistrationPhaseAndInitGame(){
        Log.d("servMan", "finishing registrationphase.");
        userListLock.readLock().lock();
        server.stopListeningForIncomingConnections(true);
        ArrayList<Player> players = new ArrayList<>();
        ArrayList<String> list = new ArrayList<>(); // for debug
        ArrayList<Spectator> spectators = new ArrayList<>();
        for(Map.Entry<String, EinzServerClientHandler> entry : registeredClientHandlers.entrySet()){
            EinzServerClientHandler handler = (EinzServerClientHandler) entry.getValue();
            String role = getRegisteredClientRoles().get(entry.getKey());
            if ( role.toLowerCase().equals("player")) {
                players.add(new Player((String) entry.getKey()));
            } else if ( role.toLowerCase().equals("spectator")){
                spectators.add(new Spectator((String) entry.getKey()));
            }
            list.add(entry.getKey());
        }
        Log.d("servMan/finishRegPhase", "Players and Spectators: "+new JSONArray(list).toString());
        userListLock.readLock().unlock();
        userListLock.writeLock().lock();
        this.gamePhaseStarted = true;
        userListLock.writeLock().unlock();
        SFLock.writeLock().lock();

        //getServerFunctionInterface().initialiseStandardGame(server, players); // returns gamestate but also modifies it internally, so i can discard the return value if I want to

//        //<Debug>
//        HashMap<Card, Integer> deck = new HashMap<>();
//        CardLoader cardLoader = EinzSingleton.getInstance().getCardLoader();
//        Card myCard = cardLoader.getCardInstance("take4");
//        deck.put(myCard, 2);
//        Collection<BasicGlobalRule> globalRules = new ArrayList<>();
//        StartGameWithCardsRule myStartGameWithCardsRule = new StartGameWithCardsRule();
//        try {
//            JSONObject param = new JSONObject();
//            param.put(StartGameWithCardsRule.getParameterName(), 7);
//            myStartGameWithCardsRule.setParameter(param);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        globalRules.add(myStartGameWithCardsRule);
//        globalRules.add(new WinOnNoCardsRule());
//        globalRules.add(new ResetCardsToDrawRule());
//        globalRules.add(new CountNumberOfCardsAsPoints());
//        globalRules.add(new NextTurnRule());
//        Map<Card, ArrayList<BasicCardRule>> cardRules = new HashMap<>();
//        HashMap<String, ArrayList<BasicCardRule>> tempCardRules = new HashMap<>(); // same as cardRules but with ID string as identifier
//        //Add all necessary CardRules
//        for (CardText ct : CardText.values()) {
//            if (ct != CardText.CHANGECOLOR && ct != CardText.CHANGECOLORPLUSFOUR && ct != CardText.DEBUG) {
//                for (CardColor cc : CardColor.values()) {
//                    if (cc != CardColor.NONE) {
//                        Card card = cardLoader.getCardInstance(cc.toString().toLowerCase() + "_" + ct.indicator);
//
//                        //deck.put(card, 1);
//
//                        //assign rules to the cards
//                        ArrayList<BasicCardRule> arr = new ArrayList<BasicCardRule>();
//                        arr.add(new PlayColorRule());
//                        arr.add(new PlayTextRule());
//                        arr.add(new IsValidDrawRule());
//                        tempCardRules.put(card.getID(), arr);
//                    }
//                }
//            } else {
//                if (ct.equals(CardText.CHANGECOLORPLUSFOUR)) {
//                    Card card = cardLoader.getCardInstance("take4");
//                    ArrayList<BasicCardRule> arr = new ArrayList<BasicCardRule>();
//                    arr.add(new PlayColorRule());
//                    arr.add(new PlayTextRule());
//                    arr.add(new IsValidDrawRule());
//                    arr.add(new PlayAlwaysRule());
//                    tempCardRules.put(card.getID(), arr);
//                } else if (ct.equals(CardText.CHANGECOLORPLUSFOUR)) {
//                    Card card = cardLoader.getCardInstance("choose");
//                    ArrayList<BasicCardRule> arr = new ArrayList<BasicCardRule>();
//                    arr.add(new PlayColorRule());
//                    arr.add(new PlayTextRule());
//                    arr.add(new IsValidDrawRule());
//                    tempCardRules.put(card.getID(), arr);
//                    arr.add(new PlayAlwaysRule());
//                }
//            }
//        }
//        for (CardColor cc : CardColor.values()) {
//
//            if (cc != CardColor.NONE) {
//                String card = cc.toString().toLowerCase() + "_" + CardText.SWITCHORDER.indicator;
//                String card1 =cc.toString().toLowerCase() + "_" + CardText.PLUSTWO.indicator;
//                String card2 =cc.toString().toLowerCase() + "_" + CardText.STOP.indicator;
//                //assign rules to the cards
//                ArrayList<BasicCardRule> arr =  tempCardRules.get(card);
//                ArrayList<BasicCardRule> arr1 = tempCardRules.get(card);
//                ArrayList<BasicCardRule> arr2 = tempCardRules.get(card2);
//                arr.add(new ChangeDirectionRule());
//                arr1.add(new DrawTwoCardsRule());
//                arr2.add(new SkipRule());
//                tempCardRules.put(card, arr);
//                tempCardRules.put(card1, arr1);
//                tempCardRules.put(card2, arr2);
//                //It might make sense to somewhere specify all IDs that exist, so that we don't have to guess
//            }
//        }
//
//        // actually add these
//        for(String id : tempCardRules.keySet()){
//            cardRules.put(cardLoader.getCardInstance(id), tempCardRules.get(id));
//        }
//
//        //getServerFunctionInterface().initialiseGame(this.server, players,deck, globalRules, cardRules);
//        getServerFunctionInterface().initialiseStandardGame(server, players);
//        //</Debug>

        if(this.latestSpecifyRulesMessageBody == null){
            getServerFunctionInterface().initialiseStandardGame(server, players);
        } else {
            initialiseNonStandardGame(latestSpecifyRulesMessageBody, server, players);
        }

        // TODO: not standard game but with rules, maybe call initialise earlier
        SFLock.writeLock().unlock();
    }

    private void initialiseNonStandardGame(EinzSpecifyRulesMessageBody specifyRulesMessageBody, ThreadedEinzServer server, ArrayList<Player> players) {
        getSFLock().writeLock().lock();
        Log.d("servMan/initNonStandardGame", "Using the rules specified instead of the standard rules.");
        // Global Rules
        Collection<BasicGlobalRule> globalRules = specifyRulesMessageBody.getGlobalParsedRules(); // these are rule objects which already have their parameters set
        // Card Rules
        Map<Card, ArrayList<BasicCardRule>> cardRules = specifyRulesMessageBody.getParsedCardRules();
        // load deck by taking every mentioned card in cardRules
        HashMap<Card, Integer> deck = specifyRulesMessageBody.getCardNumbers();

        // initialise
        getServerFunctionInterface().initialiseGame(server, players, deck, globalRules, cardRules);

        getSFLock().writeLock().unlock();
    }

    public EinzSpecifyRulesMessageBody getLatestSpecifyRulesMessageBody() {
        return latestSpecifyRulesMessageBody;
    }

    public void setLatestSpecifyRulesMessageBody(EinzSpecifyRulesMessageBody latestSpecifyRulesMessageBody) {
        this.latestSpecifyRulesMessageBody = latestSpecifyRulesMessageBody;
    }

    public void loadAndRegisterNetworkingActions(EinzActionFactory actionFactory) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        InputStream jsonStream = server.applicationContext.getResources().openRawResource(this.networkingActionFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        actionFactory.loadMappingsFromJson(jsonObject);
    }

    public void loadAndRegisterGameLogicActions(EinzActionFactory actionFactory) throws InvalidResourceFormatException, JSONException, ClassNotFoundException {
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
        Scanner s = new Scanner(is);
        s.useDelimiter("\\A");
        String ret = s.hasNext() ? s.next() : "";
        s.close();
        return ret;
    }

    /**
     * @return the ServerFunctionDefinition by Fabian. Make sure to lock {@link #SFLock} for accessing this
     */
    public ServerFunctionDefinition getServerFunctionInterface() {
        return serverFunctionInterface;
    }

    private boolean isInvalidUsername(String username){
        return (username.equals("") || username.equals("server") || username.contains("~"));
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
    public EinzMessage<? extends EinzMessageBody> registerUser(String username, String role, EinzServerClientHandler handler, JSONObject playerSeating){

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
                getRegisteredClientPositioning().putIfAbsent(username, playerSeating);
                userListLock.writeLock().unlock();
                if (DEBUG && absent != null) { // assertion on android
                    userListLock.writeLock().unlock();
                    throw new RuntimeException(new java.lang.Exception("serverManager/reg: username was absent in registeredClientHandlers but not in registeredClientRoles!"));
                }

            }
        }

        if(userListLock.writeLock().isHeldByCurrentThread())
            userListLock.writeLock().unlock();

        EinzMessage<? extends EinzMessageBody> response = null;

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
            }
        }
        getUserListLock().readLock().unlock();

        if(failureReason==null){
            getUserListLock().writeLock().lock();
            ConcurrentHashMap<String, String> clientRoles = getRegisteredClientRoles();
            ConcurrentHashMap<String, EinzServerClientHandler> clientHandlers = getRegisteredClientHandlers();

            String role = clientRoles.get(username);
            EinzServerClientHandler esch = clientHandlers.get(username);

            // inform all clients
            // broadcast UnregisterResponse
            EinzUnregisterResponseMessageBody body = new EinzUnregisterResponseMessageBody(username, unregisterReason);
            EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterResponse");
            EinzMessage<EinzUnregisterResponseMessageBody> message = new EinzMessage<>(header, body);
            broadcastMessageToAllPlayers(message);
            broadcastMessageToAllSpectators(message);

            // unregister them
            getRegisteredClientRoles().remove(username);
            getRegisteredClientHandlers().remove(username);
            getRegisteredClientPositioning().remove(username);
            getUserListLock().writeLock().unlock();

            // tell fabian about it
            if(gamePhaseStarted &&!serverShuttingDownGracefully){
                SFLock.writeLock().lock();
                if(role.equals("player")) {
                    serverFunctionInterface.removePlayer(new Player(username));
                } else if(role.equals("specator")){
                    // could inform fabian but he doesn't care
                }
                SFLock.writeLock().unlock();
            }

            //broadcast updateLobbyList
            EinzMessage<EinzUpdateLobbyListMessageBody> msg = generateUpdateLobbyListRequest();
            broadcastMessageToAllPlayers(msg);
            broadcastMessageToAllSpectators(msg);

            // and stop the corresponding client
            try {
                if(esch!=null) {
                    esch.setConnectedUser(null);
                    esch.stopThreadPatiently();
                    Log.d("servMan/unReg", "stopped Thread of " + username);
                } else {
                    Log.d("servMan/unReg", "there was no Thread of "+username+" that I could unregister");
                }
            }catch(java.lang.NullPointerException e){
                Log.d("servMan/unReg", "ESCH didn't exist anymore. (Did you maybe shut down the server?)");
                e.printStackTrace();
            }
            Log.d("servMan/unreg", "I have unregistered user "+username);

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
     * A kicked users associated thread will be stopped and the socket closed.
     * Says user is not allowed if it was not yet registered as admin. That shouldn't happen though, as messages are supposed to be ordered by client so there should always be a register first.
     * @param userToKick
     * @param userWhoIssuedThisKick
     */
    public void kickUser(String userToKick, String userWhoIssuedThisKick){
        userListLock.writeLock().lock();
        EinzServerClientHandler esch = getRegisteredClientHandlers().get(userToKick);
        // if admin is not yet set, don't kick
        boolean allowed_ = ((getAdminUsername()!=null && getAdminUsername().equals(userWhoIssuedThisKick))||userWhoIssuedThisKick.equals("server"))==true; //==true for debug info
        boolean userExists = (esch!=null);
        boolean userValid = !isInvalidUsername(userToKick);
        String unregisterReason = (userWhoIssuedThisKick.equals("server"))?"server":"kicked";

        EinzMessage<EinzKickFailureMessageBody> response;
        if(userExists && allowed_ && userValid) {
            response = unregisterUser(userToKick, unregisterReason, userWhoIssuedThisKick);
            if(response==null)//if success
            {
                userListLock.writeLock().unlock();
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
                userListLock.writeLock().unlock();
                return;
            }
        } else {
            // user doesn't exist or issuer is not admin
            EinzKickFailureMessageBody ekfmb;
            if(!allowed_){
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
        userListLock.writeLock().unlock();
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
        HashMap<String, String> localCopy = new HashMap<String, String>(chm);
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

    public ConcurrentHashMap<String, JSONObject> getRegisteredClientPositioning(){
        this.userListLock.readLock().lock();
        ConcurrentHashMap<String, JSONObject> hm = registeredClientPositioning;
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

    public void broadcastMessageToAllPlayers(EinzMessage<? extends EinzMessageBody> message) {
        try {
            Log.d("servMan/broadcastP", "broadcasting "+message.getBody().getClass().getSimpleName()+"\n"+message.toJSON().toString());
        } catch (JSONException e) {
            Log.e("servMan/broadcastP", "failed to Log that I'm broadcasting "+message.getBody().getClass().getSimpleName()+" to all players");
        }
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

    public ArrayList<String> getPlayers(){
        userListLock.readLock().lock();
        ArrayList<String> retList = new ArrayList<>();
        for(String username : getRegisteredClientRoles().keySet()){

            if(!getRegisteredClientRoles().get(username).toLowerCase().equals("player"))
                continue;

            retList.add(username);
        }
        userListLock.readLock().unlock();
        return retList;
    }

    public ArrayList<Player> getPlayersAsPlayers(){
        userListLock.readLock().lock();
        ArrayList<Player> retList = new ArrayList<>();
        for(String username : getRegisteredClientRoles().keySet()){

            if(!getRegisteredClientRoles().get(username).toLowerCase().equals("player"))
                continue;

            retList.add(new Player(username));
        }
        userListLock.readLock().unlock();
        return retList;
    }

    public ArrayList<String> getSpectators(){
        userListLock.readLock().lock();
        ArrayList<String> retList = new ArrayList<>();
        for(String username : getRegisteredClientRoles().keySet()){

            if(!getRegisteredClientRoles().get(username).toLowerCase().equals("spectator"))
                continue;

            retList.add(username);
        }
        userListLock.readLock().unlock();
        return retList;
    }

    public void broadcastMessageToAllSpectators(EinzMessage<? extends EinzMessageBody> message) {
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
        boolean b = (username!=null && !isInvalidUsername(username) && getRegisteredClientRoles().keySet().contains(username));
        return b;
    }

    public boolean isRegisteredAdmin(String username){
        boolean b = isRegistered(username);
        b = b && getAdminUsername()!=null && getAdminUsername().equals(username);
        return b;
    }

    public void startGame(String issuedByPlayer) {
        if(isRegisteredAdmin(issuedByPlayer) && !gamePhaseStarted) { // if game is running, should we restart or not?
            if(numPlayersRegisteredWithoutSpectators()>0){

            finishRegistrationPhaseAndInitGame(); //serverFunctionInterfae.initializeStandardGame is contained in this call
                setGamePhaseStarted(true);
            SFLock.writeLock().lock();
            serverFunctionInterface.startGame();
            SFLock.writeLock().unlock();}
            else{
                try {
                    sendToast(issuedByPlayer, "Please start the game only when there is at least one player.");
                } catch (UserNotRegisteredException e) {
                    Log.e("servMan/startGame", "a unregistered player glitched a bit and stopped the registration phase...\nShutting down to get rid of any potential problems");
                    e.printStackTrace();
                    server.shutdown();
                }
            }
        }
        else{
            Log.e("servMan", "somebody unauthorized tried to start the game!");
        }
    }

    private void sendToast(String issuedByPlayer, String s) throws UserNotRegisteredException {
        try {
            this.server.sendMessageToUser(issuedByPlayer, new EinzMessage<EinzShowToastMessageBody>(
                    new EinzMessageHeader("toast", "ShowToast"),
                    new EinzShowToastMessageBody(s, "server", null)
            ));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int numPlayersRegisteredWithoutSpectators() {
        userListLock.readLock().lock();
        int i=0;
        for(Map.Entry<String, EinzServerClientHandler> entry : registeredClientHandlers.entrySet()){
            EinzServerClientHandler handler = (EinzServerClientHandler) entry.getValue();
            String role = getRegisteredClientRoles().get(entry.getKey());
            if ( role.toLowerCase().equals("player")) {
                i++;
            }
        }
        userListLock.readLock().unlock();
        return i;
    }

    public void specifyRules(EinzSpecifyRulesMessageBody body) {
        getSFLock().writeLock().lock();
        //getServerFunctionInterface().initialiseGame(this.server, getPlayersAsPlayers(), body.getCardNumbers(),body.getGlobalParsedRules(), body.getParsedCardRules());
        initialiseNonStandardGame(body, server, getPlayersAsPlayers());
        getSFLock().writeLock().unlock();
    }

    /**
     * Locks to avoid inconsistencies
     */
    public void kickAllAndCloseSockets() {
        userListLock.writeLock().lock();
        for(String username : getRegisteredClientHandlers().keySet()){
            EinzServerClientHandler esch = getRegisteredClientHandlers().get(username);
            kickUser(username, "server");
            // esch.stopThreadPatiently(); is already within kickUser
        }
        userListLock.writeLock().unlock();
    }

    public void drawCards(String issuedByPlayer) {
        getSFLock().writeLock().lock();
        if(!gamePhaseStarted){
            // not allowed to draw cards!
            EinzMessage<EinzDrawCardsFailureMessageBody> message = new EinzMessage<EinzDrawCardsFailureMessageBody>(
              new EinzMessageHeader("draw", "DrawCardsFailure"),
                    new EinzDrawCardsFailureMessageBody("game not running")
            );
            try {
                server.sendMessageToUser(issuedByPlayer, message);
            } catch (UserNotRegisteredException e) {
                Log.w("servMan/drawCards", "User "+issuedByPlayer+" tried to draw a card but they weren't even registered.");
            } catch (JSONException e) {
                throw new RuntimeException(e); // if this happens, the message is corrupted. that shouldn't happen
            }

        } else {
            getServerFunctionInterface().drawCards(new Player(issuedByPlayer));
            // this will send response
        }
        getSFLock().writeLock().unlock();
    }

    public void playCard(EinzMessage message, String issuedByPlayer) {
        getSFLock().writeLock().lock();
        if(gamePhaseStarted) {
            getServerFunctionInterface().play(((EinzPlayCardMessageBody) message.getBody()).getCard(), new Player(issuedByPlayer), ((EinzPlayCardMessageBody) message.getBody()).getPlayParameters());
            // fabian sends response
        } else {
            EinzMessageHeader header = new EinzMessageHeader("playcard", "PlayCardResponse");
            EinzPlayCardResponseMessageBody body= new EinzPlayCardResponseMessageBody("false");
            EinzMessage<EinzPlayCardResponseMessageBody> response = new EinzMessage<>(header, body);
            Log.d("servMan/playCard", "Not allowing "+issuedByPlayer+" to play a card because the gamePhase hasn't started yet");
            try {
                server.sendMessageToUser(issuedByPlayer, response);
            } catch (UserNotRegisteredException e) {
                Log.w("servMan/playCard", "unregistered user "+issuedByPlayer+" tried to play a card");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        getSFLock().writeLock().unlock();
    }

    /**
     * sends the new state to the specified player. If the game is not running, the state will be null
     */
    public void onGetState(EinzMessage message, String issuedByPlayer) {
        getSFLock().readLock().lock();
        EinzMessageHeader header = new EinzMessageHeader("stateinfo", "StateInfo");

        GlobalStateParser globalState=null;
        PlayerState playerState=null;

        if(gamePhaseStarted){
            // TODO: get state from fabian
            throw new RuntimeException(new TodoException("Fabi plis inplinimt"));

        } else {

            globalState = null;
            playerState = null;

        }
        EinzSendStateMessageBody body = new EinzSendStateMessageBody(globalState, playerState);
        EinzMessage<EinzSendStateMessageBody> msg = new EinzMessage<>(header, body);
        getSFLock().readLock().unlock();
        try {
            server.sendMessageToUser(issuedByPlayer, msg);
        } catch (UserNotRegisteredException e) {
            Log.i("ServerManager/getState", "Unregistered user "+issuedByPlayer+" requested his state");
        } catch (JSONException e) {
            e.printStackTrace(); // this should not happen
        }

    }

    public void onFinishTurn(String issuedByPlayer) {
        if(gamePhaseStarted) { // ignore otherwise
            getSFLock().writeLock().lock();
            getServerFunctionInterface().finishTurn(new Player(issuedByPlayer));
            getSFLock().writeLock().unlock();
        }
    }

    public void onCustomAction(String issuedByPlayer, EinzMessage<EinzCustomActionMessageBody> message) {
        if(gamePhaseStarted){
            getSFLock().writeLock().lock();
            getServerFunctionInterface().onCustomActionMessage(issuedByPlayer, message);
            getSFLock().writeLock().unlock();

            throw new RuntimeException(new TodoException("Fabian plis implement"));
        } else {

            try {
                JSONObject failBody = new JSONObject().put("success", "false");
                EinzMessageHeader header = new EinzMessageHeader("furtheractions", "customActionResponse");
                EinzCustomActionMessageBody body = new EinzCustomActionMessageBody(failBody, message.getBody().getRuleName());
                EinzMessage<EinzCustomActionMessageBody> msg = new EinzMessage<>(header, body);
                server.sendMessageToUser(issuedByPlayer, msg);
            } catch (JSONException e) {
                e.printStackTrace();
                // shouldn't happen
            } catch (UserNotRegisteredException e) {
                e.printStackTrace();
                // who cares
            }

        }
    }

    public void setGamePhaseStarted(boolean gamePhaseStarted) {
        this.gamePhaseStarted = gamePhaseStarted;
    }
}
