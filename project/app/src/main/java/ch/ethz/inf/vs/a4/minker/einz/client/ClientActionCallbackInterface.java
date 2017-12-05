package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzRegisterSuccessAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;

public interface ClientActionCallbackInterface {

    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);

    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message);

    public void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message);

    void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message);

    // TODO implement:
    void onKickFailure (EinzMessage<EinzKickFailureMessageBody> message);
    void onInitGame (EinzMessage<EinzInitGameMessageBody> message);
    void onDrawCardsSuccess (EinzMessage<EinzDrawCardsMessageBody> message);
    void onDrawCardsFailure (EinzMessage<EinzDrawCardsFailureMessageBody> message);
    void onPlayCardResponse (EinzMessage<EinzPlayCardMessageBody> message);
    void onSendState ( EinzMessage<EinzSendStateMessageBody> message);
    // void onShowToast
    // void onPlayerfinished
    // void onGameOver
    // void CustomActionResponse
}
