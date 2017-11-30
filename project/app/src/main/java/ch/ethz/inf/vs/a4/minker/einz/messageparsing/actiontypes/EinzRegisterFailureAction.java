package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

public class EinzRegisterFailureAction extends EinzAction {
    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {

    }

    /**
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzRegisterFailureAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);

    }

    /**
     * compatibility for new actionfactory (for client)
     */
    public EinzRegisterFailureAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage<EinzRegisterFailureMessageBody> params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
        getClientActionCallbackInterface().onRegisterFailure(params);
    }
}
