package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKickMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzKickAction extends EinzAction {
    /**
     * @param sInterface
     * @param serverManager
     * @param params                must be of a Messagetype fitting to (expected by)this action
     * @param issuedByPlayer
     * @param issuedByClientHandler
     */
    public EinzKickAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler) {
        super(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }

    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        // TODO: test and map KickAction
        EinzKickMessageBody body = (EinzKickMessageBody) getMessage().getBody();
        getServerManager().kickUser(body.getUsername(), getIssuedByPlayer());
    }
}
