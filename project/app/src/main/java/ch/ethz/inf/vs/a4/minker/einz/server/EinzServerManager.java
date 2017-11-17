package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import android.util.Pair;
import ch.ethz.inf.vs.a4.minker.einz.GameState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzActionFactory;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParserFactory;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.InvalidResourceFormatException;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzFinishRegistrationPhaseAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzJsonMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

/**
 * Stores Configuration of {@link ThreadedEinzServer} that is not suited for the communications-only part, because it has to do with the content of the messages,
 * But is also not really relevant to the serverlogic
 * This also registers and handles mappings that serverlogic does not handle // TODO: register these actions maybe here, maybe in every thread
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

    public EinzServerManager(ThreadedEinzServer whotomanage, ServerFunctionDefinition serverFunctionInterface){
        this.server = whotomanage;
        this.serverFunctionInterface = serverFunctionInterface;
    }
    protected String adminUsername;
    protected ArrayList<String> getConnectedUsers(){
        return null; // TODO: move connected array from parent to here, as well as other settings
    }

    public void finishRegistrationPhaseAndStartGame(){ // TODO: disable writing to registeredClientHandlers if game started. Make everything synchronized
        Log.d("Manager", "finishing registrationphase.");
        server.stopListeningForIncomingConnections(true);
        ArrayList<Player> players = new ArrayList<>();
        synchronized (server.getRegisteredClientHandlers()) {
            for(Map.Entry entry : this.server.getRegisteredClientHandlers().entrySet()){
                Pair<EinzServerClientHandler, Thread> pair = (Pair) entry.getValue();
                players.add(new Player((String) entry.getKey()));
                // TODO: send that info to clients
            }
            Log.d("Manager/finishRegPhase", "Players: "+players.toString());
            GameState gameState = getServerFunctionInterface().startStandartGame(players); // returns gamestate but also modifies it internally, so i can discard the return value if I want to

        }
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

    // TODO: register players
}
