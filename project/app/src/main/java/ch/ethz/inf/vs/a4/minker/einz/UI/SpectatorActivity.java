package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzGameOverMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzInitGameMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKickFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayerFinishedMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSendStateMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzShowToastMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

public class SpectatorActivity extends FullscreenActivity implements GameUIInterface{

    private EinzClient ourClient;
    private ArrayList<Card> cardStack = new ArrayList<>();
    private ImageView trayStack,trayStack2;
    private ImageView drawPile,drawPile2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectator);

        this.ourClient = EinzSingleton.getInstance().getEinzClient();
        ourClient.getActionCallbackInterface().setGameUI(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(ourClient!=null && ourClient.getActionCallbackInterface()!=null)
            ourClient.getActionCallbackInterface().setGameUI(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ourClient!=null && ourClient.getActionCallbackInterface()!=null)
            ourClient.getActionCallbackInterface().setGameUI(this);
    }

    @Override
    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message) {

    }

    @Override
    public void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message) {

    }

    @Override
    public void onShowToast(EinzMessage<EinzShowToastMessageBody> message) {

    }

    @Override
    public void onKickFailure(EinzMessage<EinzKickFailureMessageBody> message) {

    }

    @Override
    public void onDrawCardsSuccess(EinzMessage<EinzDrawCardsSuccessMessageBody> message) {

    }

    @Override
    public void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message) {

    }

    @Override
    public void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message) {

    }

    @Override
    public void onSendState(EinzMessage<EinzSendStateMessageBody> message) {

    }

    @Override
    public void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message) {

    }

    @Override
    public void onCustomActionResponse(EinzMessage<EinzCustomActionResponseMessageBody> message) {

    }

    @Override
    public void onGameOver(EinzMessage<EinzGameOverMessageBody> message) {

    }

    @Override
    public void setHand(ArrayList<Card> hand) {

    }

    @Override
    public void setActions(ArrayList<String> actions) {

    }

    @Override
    public void playerStartedTurn(String playerThatStartedTurn) {

    }

    @Override
    public void onInitGame(EinzMessage<EinzInitGameMessageBody> message) {

    }

    @Override
    public void setNumCardsInHandOfEachPlayer(HashMap<String, String> numCardsInHandOfEachPlayer) {

    }

    @Override
    public void onUpdateLobbyList(String admin, ArrayList<String> players, ArrayList<String> spectators) {

    }

    @Override
    public void setStack(ArrayList<Card> stack) {

    }

    @Override
    public void onKeepaliveTimeout() {

    }
}
