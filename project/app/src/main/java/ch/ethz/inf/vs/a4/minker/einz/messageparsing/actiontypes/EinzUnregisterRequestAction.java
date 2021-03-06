package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.BuildConfig;
import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

public class EinzUnregisterRequestAction extends EinzAction {
    /**
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzUnregisterRequestAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }

    /**
     * compatibility for new actionfactory (for client)
     */
    public EinzUnregisterRequestAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        Log.d("UnregReqAction", "starting run()");
        EinzUnregisterRequestMessageBody body = ((EinzUnregisterRequestMessageBody) this.getMessage().getBody());
        if(getIssuedByPlayer() == null){
            // A ESCH tried to do this without having a registered user. Don't do that
            Log.w("UnregReqAction","EinzServerClientHandler without registered username tried to run this");
            return;
        }
        // validate that the user is allowed to do this
        EinzMessage response;
        if(getIssuedByPlayer().equals(body.getUsername())) {
            response = getServerManager().unregisterUser(body.getUsername(), "disconnected", body.getUsername());
            if(BuildConfig.DEBUG && response != null){
                //some failure occurred but we didn't expect that for unregistering ourselves, only for kicking
                // but then again, we didn't want to use this return anyways
            }

            // don't send response back, 'response' here is null except for kicking
            // unregisterUser automatically broadcasts UnregisterResponse

        } else {
            // user is not allowed to do this, just don't react
            Log.w("UnregReqAction","User "+getIssuedByPlayer()+" is not allowed to unregister "+body.getUsername());
        }
    }
}
