package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzRegisterSuccessAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterSuccessMessageBody;

public interface ClientActionCallbackInterface {
    // TODO: add the function you use
    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);
}
