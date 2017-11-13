package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzPlayCardAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

public class EinzActionFactory {

    private ServerFunctionDefinition sInterface;

    public EinzActionFactory(ServerFunctionDefinition serverFunctionDefinition){
        this.sInterface = serverFunctionDefinition;
    }

    public EinzAction<EinzPlayCardMessageBody> generateEinzAction(EinzMessage<EinzPlayCardMessageBody> message){ // using type of message to determine action. Using overloading for this
        return new EinzPlayCardAction(sInterface, message);
    }

    // TODO: implement for all other actions, including default if bad message
}
