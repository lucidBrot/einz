package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;

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
     * compatibility for new actionfactory (for client)
     */
    public EinzRegisterAction(ServerFunctionDefinition sInterface, EinzServerManager serverManager, EinzMessage params, String issuedByPlayer, EinzServerClientHandler issuedByClientHandler, ClientActionCallbackInterface clientActionCallbackInterface, Object completelyCustom){
        this(sInterface, serverManager, params, issuedByPlayer, issuedByClientHandler);
    }

    /**
     * executes the action in the current thread.
     * That is, it registers the User for this clientHandler and then triggers a response
     */
    @Override
    public void run() {
        // register user
        EinzMessage response = getServerManager().registerUser(this.body.getUsername(), this.body.getRole(), getEinzServerClientHandler());
        // send response
        getEinzServerClientHandler().sendMessage(response);
        // if it was a successful register, inform all clients about the change
        if(response.getBody() instanceof EinzRegisterSuccessMessageBody) {
            EinzMessage<EinzUpdateLobbyListMessageBody> msg = getServerManager().generateUpdateLobbyListRequest();
            getServerManager().broadcastMessageToAllPlayers(msg);
            getServerManager().broadcastMessageToAllSpectators(msg);
        }
    }
}
