package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

/**
 * Specifies that an EinzAction should have a function run which will run the action
 */
public abstract class EinzAction {

    /**
     * executes the action in the current thread
     */
    public abstract void run();

    private final ServerFunctionDefinition sInterface;
    private final EinzMessage message; // This has a specific messagebody in every subclass
    private final String issuedByPlayer; // CAN BE NULL if user is not registered or not known and irrelevant
    private final EinzServerManager serverManager;

    public EinzAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer){
        this.sInterface = sInterface;
        this.message = params;
        this.issuedByPlayer = issuedByPlayer;
        this.serverManager = serverManager;
    }

    public ServerFunctionDefinition getsInterface() {
        return sInterface;
    }

    public EinzMessage getMessage() {
        return message;
    }

    public String getIssuedByPlayer() {
        return issuedByPlayer;
    }

    public EinzServerManager getServerManager() {
        return serverManager;
    }
}
