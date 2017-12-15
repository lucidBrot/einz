package ch.ethz.inf.vs.a4.minker.einz.UI;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

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

    //void onInitGame(EinzMessage<EinzInitGameMessageBody> message);

    void onDrawCardsSuccess(EinzMessage<EinzDrawCardsSuccessMessageBody> message);

    void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message);

    void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message);

    void onSendState(EinzMessage<EinzSendStateMessageBody> message);

    void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message);

    void onCustomActionResponse(EinzMessage<EinzCustomActionResponseMessageBody> message);

    void onGameOver (EinzMessage<EinzGameOverMessageBody> message);

    void setHand(ArrayList<Card> hand);

    void setActions(ArrayList<String> actions);

    void playerStartedTurn(String playerThatStartedTurn);

    void onInitGame(EinzMessage<EinzInitGameMessageBody> message);

    void setNumCardsInHandOfEachPlayer(HashMap<String ,Integer> numCardsInHandOfEachPlayer);

    void onUpdateLobbyList(String admin, ArrayList<String> players, ArrayList<String> spectators);

    void setStack(ArrayList<Card> stack);

    void onKeepaliveTimeout();

    void onSendPlayParameters(JSONObject playParameters);

    // TODO: customActionRule..?
}
