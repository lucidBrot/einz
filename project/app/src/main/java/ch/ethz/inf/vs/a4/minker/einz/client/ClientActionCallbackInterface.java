package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzRegisterSuccessAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;

public interface ClientActionCallbackInterface {

    void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);

    void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message);

    void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message);

    void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message);

    void onShowToast(EinzMessage<EinzShowToastMessageBody> message);

    // below are not yet implemented (05.12.2017)

    void onKickFailure(EinzMessage<EinzKickFailureMessageBody> message);

    void onInitGame(EinzMessage<EinzInitGameMessageBody> message);

    void onDrawCardsSuccess(EinzMessage<EinzDrawCardsMessageBody> message);

    void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message);

    void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message);

    void onSendState(EinzMessage<EinzSendStateMessageBody> message);

    void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message);

    void onGameOver(EinzMessage<EinzGameOverMessageBody> message);

    void onCustomActionResponse(EinzMessage<EinzCustomActionResponseMessageBody> message);
}
