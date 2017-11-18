package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzRegisterAction extends EinzAction{

    private final EinzMessage params;
    private final EinzRegisterMessageBody body;

    /**
     * @param sInterface
     * @param serverManager
     * @param params some {@link EinzMessage} featuring {@link ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody}
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzRegisterAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage<EinzRegisterMessageBody> params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
        this.params = params;
        this.body = params.getBody();
    }

    /**
     * executes the action in the current thread.
     * That is, it registers the User for this clientHandler and then triggers a response
     */
    @Override
    public void run() {
        getServerManager().registerUser(this.body.getUsername(), getEinzServerClientHandler()); // TODO: response on register
    }
}
