package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzFinishRegistrationPhaseAction extends EinzAction {
    public EinzFinishRegistrationPhaseAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer) {
        super(sInterface, serverManager, params, issuedByPlayer);
    }

    @Override
    public void run() {
        this.getServerManager().finishRegistrationPhaseAndStartGame();
    }
}
