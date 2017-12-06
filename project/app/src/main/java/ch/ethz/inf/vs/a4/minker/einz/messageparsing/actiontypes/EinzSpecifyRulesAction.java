package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

public class EinzSpecifyRulesAction extends EinzAction {

    private final EinzSpecifyRulesMessageBody body;
    /**
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzSpecifyRulesAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        this(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, null, null);
    }

    /**
     * compatibility for new actionfactory (for client)
     */
    public EinzSpecifyRulesAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
        this.body = (EinzSpecifyRulesMessageBody) params.getBody();
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        if(getServerManager().isRegisteredAdmin(getIssuedByPlayer())){
            // if admin and registered aka allowed to specify rules
            getServerManager().specifyRules(body.getCardRules(), body.getGlobalRules());
        } else {
            String admin = getServerManager().getAdminUsername();
            admin = (admin==null)?"null":admin;
            Log.w("specifyRulesAction", "Fuck off, "+getIssuedByPlayer()+". You're not allowed to specify rules. (admin="+admin+")");
        }
    }
}
