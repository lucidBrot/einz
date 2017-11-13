package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzRegisterAction extends EinzAction{

    public EinzRegisterAction(ServerFunctionDefinition sInterface, EinzMessage params, String issuedByPlayer) {
        super(sInterface, params, issuedByPlayer);
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {

    }
}
