package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.UI.GameUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public interface ClientActionCallbackInterface {

    void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message);

    void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message);

    void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message);

    void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message);

    void onShowToast(EinzMessage<EinzShowToastMessageBody> message);

    // below are not yet implemented (05.12.2017)

    void onKickFailure(EinzMessage<EinzKickFailureMessageBody> message);

    void onInitGame(EinzMessage<EinzInitGameMessageBody> message);

    void onDrawCardsSuccess(EinzMessage<EinzDrawCardsSuccessMessageBody> message);

    void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message);

    void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message);

    void onSendState(EinzMessage<EinzSendStateMessageBody> message);

    void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message);

    void onGameOver(EinzMessage<EinzGameOverMessageBody> message);

    void onCustomActionResponse(EinzMessage<EinzCustomActionResponseMessageBody> message);

    void onKeepaliveTimeout();

    void setGameUIAndDisableLobbyUI(GameUIInterface gameUI);
    void setGameUI(GameUIInterface gameUI);
    void setLobbyUI(LobbyUIInterface lobbyUI);
    HashMap<String, JSONObject> getPlayerSeatings();
    void sendSpecifyRules(JSONObject cardRules, JSONArray globalRules);
}
