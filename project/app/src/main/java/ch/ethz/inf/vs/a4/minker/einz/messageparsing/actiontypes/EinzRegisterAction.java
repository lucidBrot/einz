package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzRegisterAction extends EinzAction{

    public EinzRegisterAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer) {
        super(sInterface, serverManager, params, issuedByPlayer);
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        Log.d("Action/Register", "was run");
        this.//TODO: store connected player in servermanager
    }
}
