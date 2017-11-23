package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

public class EinzPlayCardAction extends EinzAction {
    public EinzPlayCardAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }
    // require the Type of messagebody to fit to this class here


    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        Log.d("EinzPlayCardAction", "run()");
        this.getsInterface().play(((EinzPlayCardMessageBody) this.getMessage().getBody()).getCard(), new Player(
                    ""
            )); // TODO: update this when the interface has updated
    }
}
