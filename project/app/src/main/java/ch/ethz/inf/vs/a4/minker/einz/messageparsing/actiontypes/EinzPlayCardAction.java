package ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzPlayCardAction extends EinzAction<EinzPlayCardMessageBody> { // require the Type of messagebody to fit to this class here
    private EinzPlayCardMessageBody messagebody;

    public EinzPlayCardAction(ServerFunctionDefinition sInterface, EinzMessage<EinzPlayCardMessageBody> params) {
        super(sInterface, params);
        this.messagebody = (EinzPlayCardMessageBody) this.message.body;
    }

    @Override
    public void run(Player player) {
        this.sInterface.play(this.messagebody.getCard(), player);
    }
}
