package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collections;
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
    private final double cardSizeRatio = 351.0/251.0;
    private ArrayList<String> allPlayers = new ArrayList<>();
    private ArrayList<Card> stack = new ArrayList<>();

    private String currentlyActivePlayer = "~";
    private HashMap<String,String> playerDirections = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectator);

        trayStack = findViewById(R.id.tray_stack);

        trayStack2 = findViewById(R.id.tray_stack_2);

        this.ourClient = EinzSingleton.getInstance().getEinzClient();
        ourClient.getActionCallbackInterface().setGameUI(this);
    }

    public void addPlayerToList(String addedPlayer){
        //ensure no player is added twice
        if(!allPlayers.contains(addedPlayer)) {
            GridLayout playerList = findViewById(R.id.gl_playerlist);

            CardView usercard = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_playerlist_element, playerList, false);
            // false because don't add view yet - I first want to set some text

            TextView tv_username = usercard.findViewById(R.id.tv_playerlist_username);

            // set text
            tv_username.setText(addedPlayer);

            // highlight yourself
            /*
            if(player.equals(ourClient.getUsername())){
                usercard.setCardBackgroundColor(getResources().getColor(R.color.red_default));
                ((ImageView)usercard.findViewById(R.id.icn_role)).setColorFilter(getResources().getColor(R.color.red_darker));
                ((ImageView)usercard.findViewById(R.id.btn_lobby_kick)).setColorFilter(getResources().getColor(R.color.red_darker));
                ((TextView)usercard.findViewById(R.id.tv_lobbylist_username)).setTextColor(getResources().getColor(R.color.red_darker));
                ((TextView)usercard.findViewById(R.id.tv_lobbylist_role)).setTextColor(getResources().getColor(R.color.red_darker));
            }*/

            // add view
            usercard.setTag(addedPlayer);
            playerList.addView(usercard);
            allPlayers.add(addedPlayer);
        }
    }

    public boolean checkCardListIdentical(ArrayList<Card> cardlist1,ArrayList<Card> cardlist2){
        if(cardlist1.size() != cardlist2.size()){
            return false;
        }

        ArrayList<String> stringOfOwnCards = new ArrayList<>();
        ArrayList<String> stringOfGotCards = new ArrayList<>();

        for(Card currCard:cardlist2){
            stringOfOwnCards.add(currCard.getID());
        }

        for(Card currCard:cardlist1){
            stringOfGotCards.add(currCard.getID());
        }

        Collections.sort(stringOfGotCards);
        Collections.sort(stringOfOwnCards);

        return(stringOfGotCards.equals(stringOfOwnCards));
    }

    public void setTopPlayedCard(Card cardToSet) {

        //((BitmapDrawable)trayStack.getDrawable()).getBitmap().recycle();

        Bitmap b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()))).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, trayStack.getWidth(),(int)(cardSizeRatio * (double)trayStack.getWidth()), false);
        trayStack.setImageBitmap(bitmapResized);

        double direction = Math.random() * 2*Math.PI;
        double xTranslation = Math.cos(direction) * 1000;
        double yTranslation = Math.sin(direction) * 1000;

        trayStack.animate().translationX((int)xTranslation).translationY((int)yTranslation).setDuration(0).setInterpolator(new AccelerateDecelerateInterpolator()).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                trayStack.animate().translationX(0).translationY(0).setDuration(1000);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public void setSecondTopPlayedCard(Card cardToSet) {

        //((BitmapDrawable)trayStack.getDrawable()).getBitmap().recycle();

        Bitmap b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()))).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, trayStack2.getWidth(),(int)(cardSizeRatio * (double)trayStack2.getWidth()), false);
        trayStack2.setImageBitmap(bitmapResized);

        //setlastplayedCard(cardToSet);
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
        currentlyActivePlayer = playerThatStartedTurn;
        if(allPlayers.contains(playerThatStartedTurn)) {
            android.widget.GridLayout playerList = findViewById(R.id.gl_playerlist);

            for (int i = 0; i < playerList.getChildCount();i++) {
                View v = playerList.getChildAt(i);

                if (v instanceof CardView && v.getTag().equals(playerThatStartedTurn)){
                    // Do something
                    ((CardView) v).setCardBackgroundColor(getResources().getColor(R.color.blue_dark));
                } else if(v instanceof CardView){
                    ((CardView) v).setCardBackgroundColor(getResources().getColor(R.color.blue_default));
                }
            }
        }
    }

    @Override
    public void onInitGame(EinzMessage<EinzInitGameMessageBody> message) {
        ArrayList<String> playerList = message.getBody().getTurnOrder();
        for(String currPlayer:playerList){
            addPlayerToList(currPlayer);
        }
    }

    @Override
    public void setNumCardsInHandOfEachPlayer(HashMap<String, String> numCardsInHandOfEachPlayer) {
        for (String currPlayer:allPlayers){
            String numOfCurrplayerCards = numCardsInHandOfEachPlayer.get(currPlayer);
            android.widget.GridLayout playerList = findViewById(R.id.gl_playerlist);
            View cardViewOfPlayer = playerList.findViewWithTag(currPlayer);

            if(cardViewOfPlayer instanceof CardView){
                View textViewOfNrOfCards = cardViewOfPlayer.findViewById(R.id.tv_nr_of_cards);
                if(textViewOfNrOfCards instanceof TextView){
                    ((TextView) textViewOfNrOfCards).setText(numOfCurrplayerCards);
                }
            } else if(cardViewOfPlayer == null){
                /*
                removePlayerFromList(currPlayer);
                addPlayerToList(currPlayer);*/
            }
        }
    }

    @Override
    public void onUpdateLobbyList(String admin, ArrayList<String> players, ArrayList<String> spectators) {

    }

    @Override
    public void setStack(ArrayList<Card> stack) {
        if(!checkCardListIdentical(stack,cardStack)) {
            Card sndCard = null;

            if (stack.size() > 1) {
                sndCard = stack.get(stack.size() - 2);
                setSecondTopPlayedCard(sndCard);
            }

            final Card topCard = stack.get(stack.size() - 1);

            setTopPlayedCard(topCard);

            cardStack = stack;
        }
    }

    @Override
    public void onKeepaliveTimeout() {

    }
}
