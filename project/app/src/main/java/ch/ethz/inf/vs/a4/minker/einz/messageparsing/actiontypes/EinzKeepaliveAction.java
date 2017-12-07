package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;

public class EinzKeepaliveAction extends EinzAction {
    /**
     * Only needs the params your action needs. null is possibly fine
     *
     * @param sInterface
     * @param serverManager
     * @param params                        must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     * @param clientActionCallbackInterface
     * @param completelyCustom
     */
    public EinzKeepaliveAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
    }

    /**
     * Only needs the params your action needs. null is possibly fine.
     * Backwards compatibility for serverside actions. Client cannot use this.
     *
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzKeepaliveAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        if(Debug.logKeepalivePackets){
            Log.d("KeepaliveAction", ".");
        }
    }
}
