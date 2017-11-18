package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzShowToastAction extends EinzAction {
    public EinzShowToastAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer) {
        super(sInterface, serverManager, params, issuedByPlayer);
    }

    @Override
    public void run() {

    }
}
