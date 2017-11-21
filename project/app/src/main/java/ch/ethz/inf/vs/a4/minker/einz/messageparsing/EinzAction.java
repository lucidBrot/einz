package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

/**
 * Specifies that an EinzAction should have a function run which will run the action.
 * Make sure to check whether IssuedByPlayer is null - that probably means that the user is not registered.
 */
public abstract class EinzAction {

    /**
     * executes the action in the current thread
     */
    public abstract void run();

    private final ServerFunctionDefinition sInterface;
    private final EinzMessage message; // This has a specific messagebody in every subclass
    private final String issuedByPlayer; // CAN BE NULL if user is not registered
    private final EinzServerManager serverManager;
    private final EinzServerClientHandler einzServerClientHandler;

    /**
     * @param sInterface
     * @param serverManager
     * @param params must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler){
        this.sInterface = sInterface;
        this.message = params;
        this.issuedByPlayer = issuedByPlayer;
        this.serverManager = serverManager;
        einzServerClientHandler = issuedByClientHandler;
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

    public EinzServerClientHandler getEinzServerClientHandler() {
        return einzServerClientHandler;
    }
}
