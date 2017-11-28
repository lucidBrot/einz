package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzRegisterSuccessAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;

public interface ClientActionCallbackInterface {
    // TODO: add the functions you use
    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);

    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message);

    public void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message);
}
