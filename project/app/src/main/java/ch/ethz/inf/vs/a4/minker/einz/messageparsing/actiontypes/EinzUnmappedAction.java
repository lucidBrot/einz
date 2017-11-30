package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnmappedMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import org.json.JSONException;

public class EinzUnmappedAction extends EinzAction {
    private final EinzMessage message;
    private final String messageString;
    /**
     *
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzUnmappedAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        this(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, null, null);

    }

    /**
     * compatibility for new actionfactory (for client)
     */
    public EinzUnmappedAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler, clientActionCallbackInterface, completelyCustom);
        this.message = getMessage();
        String m;
        try {
            m = (this.message==null)?"null":message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            m="not set";
        }
        messageString=m;
    }

    @Override
    public void run() {
        Log.w("unmappedAction", "Unmapped Action was run. Message: "+messageString);
    }
}
