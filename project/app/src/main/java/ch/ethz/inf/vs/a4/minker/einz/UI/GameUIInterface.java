package ch.ethz.inf.vs.a4.minker.einz.UI;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;

/**
 * The methods that the Game Activity should implement
 */
public interface GameUIInterface {
    //commented out with three slashes should not happen during the game phase
    ///void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);

    void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message);

    ///void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message);

    void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message);

    void onShowToast(EinzMessage<EinzShowToastMessageBody> message);

    void onKickFailure(EinzMessage<EinzKickFailureMessageBody> message);

    ///void onInitGame(EinzMessage<EinzInitGameMessageBody> message);

    void onDrawCardsSuccess(EinzMessage<EinzDrawCardsMessageBody> message);

    void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message);

    void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message);

    void onSendState(EinzMessage<EinzSendStateMessageBody> message);

    void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message);

    void onCustomActionResponse(EinzMessage<EinzCustomActionMessageBody> message);

    void onGameOver (EinzMessage<EinzGameOverMessageBody> message);

    // TODO: customActionRule..?
}
