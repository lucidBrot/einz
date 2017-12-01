package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;

/**
 * Created by silvia on 11/24/17.
 */

public class EinzCustomActionAction extends EinzAction {

    public EinzCustomActionAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
    }

    public EinzCustomActionAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }

    @Override
    public void run() {
        getServerManager().onCustomAction(getIssuedByPlayer(), getMessage());
    }
}
