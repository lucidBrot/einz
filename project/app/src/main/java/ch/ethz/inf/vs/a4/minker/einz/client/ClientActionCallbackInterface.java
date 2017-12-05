package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzRegisterSuccessAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;

public interface ClientActionCallbackInterface {
    // TODO: add the functions you use
    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);

    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message);

    public void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message);

    void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message);

    // TODO implement:
    void onKickFailure (EinzMessage<EinzKickFailureMessageBody> message);
    void onInitGame (EinzMessage<EinzInitGameMessageBody> message);
}
