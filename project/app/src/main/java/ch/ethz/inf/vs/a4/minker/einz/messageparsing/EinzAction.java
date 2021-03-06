package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

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
    private final ClientActionCallbackInterface clientActionCallbackInterface;
    private final Object completelyCustom;

    /**
     * Only needs the params your action needs. null is possibly fine
     * @param sInterface
     * @param serverManager
     * @param params must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     * @param clientActionCallbackInterface
     * @param completelyCustom
     */
    public EinzAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        this.sInterface = sInterface;
        this.message = params;
        this.issuedByPlayer = issuedByPlayer;
        this.serverManager = serverManager;
        this.einzServerClientHandler = issuedByClientHandler;
        this.completelyCustom = completelyCustom;
        this.clientActionCallbackInterface = clientActionCallbackInterface;
    }

    /**
     * Only needs the params your action needs. null is possibly fine.
     * Backwards compatibility for serverside actions. Client cannot use this.
     * @param sInterface
     * @param serverManager
     * @param params must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler){
        this(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, null, null);
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

    public ClientActionCallbackInterface getClientActionCallbackInterface() {
        return clientActionCallbackInterface;
    }

    public Object getCompletelyCustom() {
        return completelyCustom;
    }
}
