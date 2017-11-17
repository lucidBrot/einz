package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import android.util.Pair;
import ch.ethz.inf.vs.a4.minker.einz.GameState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzActionFactory;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzFinishRegistrationPhaseAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzJsonMessageBody;

import java.util.ArrayList;
import java.util.Map;

/**
 * Stores Configuration of {@link ThreadedEinzServer} that is not suited for the communications-only part, because it has to do with the content of the messages,
 * But is also not really relevant to the serverlogic
 * This also registers and handles mappings that serverlogic does not handle // TODO: register these actions maybe here, maybe in every thread
 */
public class EinzServerManager {

    private final ThreadedEinzServer server;
    private ServerFunctionDefinition serverFunctionInterface;

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
            GameState gameState = getServerFunctionInterface().startStandartGame(players); // DEBUG
            Log.d("Manager/finishRegPhase", "Now what do I do with the gamestate "+gameState.toString()+" ?");
        }
    }

    public void registerNetworkingActions(EinzActionFactory actionFactory){ //TODO: register networking actions, maybe from json file

    }

    public void registerGameLogicActions(EinzActionFactory actionFactory){ //TODO: register actions for game logic. from different json file

    }

    public ServerFunctionDefinition getServerFunctionInterface() {
        return serverFunctionInterface;
    }

    public void setServerFunctionInterface(ServerFunctionDefinition serverFunctionInterface) {
        this.serverFunctionInterface = serverFunctionInterface;
    }

    // TODO: register players
}
