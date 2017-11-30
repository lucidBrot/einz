package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

public class EinzUpdateLobbyListAction extends EinzAction {
    /**
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzUpdateLobbyListAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage<EinzUpdateLobbyListMessageBody> params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }

    /**
     * compatibility for new actionfactory (for client)
     */
    public EinzUpdateLobbyListAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage<EinzUpdateLobbyListMessageBody> params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        getClientActionCallbackInterface().onUpdateLobbyList(getMessage());
    }
}
