package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivity;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzPlayCardAction extends EinzAction<EinzPlayCardMessageBody> { // require the Type of messagebody to fit to this class here
    private EinzPlayCardMessageBody messagebody;

    public EinzPlayCardAction(ServerFunctionDefinition sInterface, EinzMessage<EinzPlayCardMessageBody> params, String issuedByPlayer) {
        super(sInterface, params, issuedByPlayer);
        this.messagebody = (EinzPlayCardMessageBody) this.message.getBody();
    }


    /**
     * executes the action in the current thread
     */
    @Override
    public void run() {
        this.sInterface.play(this.messagebody.getCard(), new Player(
                    this.issuedByPlayer,
                    null
            )); // TODO: update this when the interface has updated
    }
}
