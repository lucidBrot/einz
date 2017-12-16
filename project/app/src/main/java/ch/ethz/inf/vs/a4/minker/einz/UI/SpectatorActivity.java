package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

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
import ch.ethz.inf.vs.a4.minker.einz.sensors.OrientationGetter;

public class SpectatorActivity extends FullscreenActivity implements GameUIInterface{

    private EinzClient ourClient;
    private ArrayList<Card> cardStack = new ArrayList<>();
    private ImageView trayStack,trayStack2;
    private ImageView drawPile,drawPile2;
    private final double cardSizeRatio = 351.0/251.0;
    private ArrayList<String> allPlayers = new ArrayList<>();
    private ArrayList<Card> stack = new ArrayList<>();

    private String currentlyActivePlayer = "~";
    private HashMap<String, String> playerDirections = new HashMap<>();
    private HashMap<String, JSONObject> playerSeating = new HashMap<>();
    private HashMap<String,Double> orientationOfPlayer = new HashMap<>();
    private String frameColor = "NONE";
    private ImageView colorBorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spectator);
        Button endGame = findViewById(R.id.btn_end_game);
        endGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackToMainMenu();
            }
        });

        trayStack = findViewById(R.id.tray_stack_spec);
        colorBorder = findViewById(R.id.iv_wished_color);

        trayStack2 = findViewById(R.id.tray_stack_spec_2);

        this.ourClient = EinzSingleton.getInstance().getEinzClient();

        //ourClient.getActionCallbackInterface().setGameUI(this);

        // this.ourClient.getActionCallbackInterface().getPlayerSeatings()
    }

    private void goBackToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

            usercard.getLayoutParams().width = playerList.getMeasuredWidth()/2;

            usercard.getLayoutParams().width -= ((CardView.MarginLayoutParams)usercard.getLayoutParams()).leftMargin + ((CardView.MarginLayoutParams)usercard.getLayoutParams()).rightMargin;

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

        Bitmap b = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()), getApplicationContext().getTheme())).getBitmap();
        } else {
            b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()))).getBitmap();
        }

        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, trayStack.getWidth(),(int)(cardSizeRatio * (double)trayStack.getWidth()), false);
        trayStack.setImageBitmap(bitmapResized);


        //double specOrientation = Double.NaN,playerOrientation = Double.NaN;

        /*
        if(playerSeating != null) {
            specOrientation = JSONToOrientation(playerSeating.get(ourClient.getUsername()));
            playerOrientation = JSONToOrientation(playerSeating.get(currentlyActivePlayer));
        }
        */

        /*
        double direction;
        if(Double.isNaN(specOrientation) || Double.isNaN(playerOrientation)){
            direction = Math.random() * 2*Math.PI;
        } else {
            direction = (Math.PI + playerOrientation) - specOrientation;
        }
        */

        double direction;

        if(orientationOfPlayer.containsKey(currentlyActivePlayer) && !Double.isNaN(orientationOfPlayer.get(currentlyActivePlayer))) {
            direction = orientationOfPlayer.get(currentlyActivePlayer);
        } else {
            direction = Math.random() * 2 * Math.PI;
        }
        double xTranslation = Math.cos(direction) * 1500;
        double yTranslation = Math.sin(direction) * 1500;

        Log.d("Translation","x: " + String.valueOf(xTranslation) + " y: "+yTranslation);

        trayStack.setVisibility(View.GONE);
        colorBorder.setVisibility(View.GONE);

        trayStack.animate().translationX((int)xTranslation).translationY((int)yTranslation).setDuration(0).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                trayStack.setVisibility(View.VISIBLE);
                trayStack.animate().translationX(0).translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        setFrameColor();
                        trayStack2.setImageBitmap(bitmapResized);
                    }
                });

            }
        });
    }

    private void setFrameColor() {
        switch (frameColor){
            case "GREEN":
                colorBorder.setVisibility(View.VISIBLE);
                colorBorder.setImageResource(R.drawable.card_border_green);
                break;
            case "RED":
                colorBorder.setVisibility(View.VISIBLE);
                colorBorder.setImageResource(R.drawable.card_border_red);
                break;
            case "BLUE":
                colorBorder.setVisibility(View.VISIBLE);
                colorBorder.setImageResource(R.drawable.card_border_blue);
                break;
            case "YELLOW":
                colorBorder.setVisibility(View.VISIBLE);
                colorBorder.setImageResource(R.drawable.card_border_yellow);
                break;
            default:

                break;
        }
    }

    private boolean isValidColor(String inColor) {
        inColor = inColor.toUpperCase();
        return inColor.equals("BLUE") || inColor.equals("GREEN") || inColor.equals("YELLOW") || inColor.equals("RED");
    }

    public void setSecondTopPlayedCard(Card cardToSet) {

        //((BitmapDrawable)trayStack.getDrawable()).getBitmap().recycle();

        /*Bitmap b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()))).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, trayStack2.getWidth(),(int)(cardSizeRatio * (double)trayStack2.getWidth()), false);
        trayStack2.setImageBitmap(bitmapResized);*/
        trayStack2.setImageDrawable(trayStack.getDrawable());

        //setlastplayedCard(cardToSet);
    }

    private double JSONToOrientation(JSONObject orientationObject){
        return orientationObject.optDouble("orientation");
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        Log.d("DEBUG", "toost updateLobby");
        playerSeating = message.getBody().getPlayerSeatings();

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
        LinearLayout endscreen = findViewById(R.id.ll_endscreen);
        endscreen.setVisibility(View.VISIBLE);
        HashMap<String, String> playerPoints = message.getBody().getPoints();

        String winner = "MissingNo";
        int winnerPoints = Integer.MAX_VALUE;
        for (String player : allPlayers) {

            LinearLayout winningPlayers = findViewById(R.id.ll_winning_players);

            CardView usercard = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_playerpointlist_element, winningPlayers, false);
            // false because don't add view yet - I first want to set some text

            TextView tv_username = usercard.findViewById(R.id.tv_playerlist_username);
            TextView tv_points = usercard.findViewById(R.id.tv_nr_of_points);

            // set text
            tv_username.setText(player);
            String points = playerPoints.get(player);
            int intPoints = Integer.parseInt(points);
            tv_points.setText(playerPoints.get(player));
            usercard.setTag(intPoints);

            if(intPoints < winnerPoints){
                winner = player;
                winnerPoints = intPoints;
            }

            // add view
            //winningPlayers.addView(usercard);
            if(winningPlayers.getChildCount() > 0) {
                for (int i = 0; i < winningPlayers.getChildCount(); i++) {
                    View v = winningPlayers.getChildAt(i);
                    if (intPoints < (int) v.getTag()) {
                        winningPlayers.addView(usercard, i);
                        break;
                    } else if (i == winningPlayers.getChildCount() - 1) {
                        winningPlayers.addView(usercard);
                        break;
                    }
                }
            } else {
                winningPlayers.addView(usercard);
            }
        }
        ((TextView)findViewById(R.id.tv_winner)).setText(winner + " won!");
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
            GridLayout playerList = findViewById(R.id.gl_playerlist);

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
            double orientation = Math.random() * 2 * Math.PI;
            orientationOfPlayer.put(currPlayer,orientation);
        }
    }

    @Override
    public void setNumCardsInHandOfEachPlayer(HashMap<String, Integer> numCardsInHandOfEachPlayer) {
        for (String currPlayer:allPlayers){
            Integer numOfCurrplayerCards = numCardsInHandOfEachPlayer.get(currPlayer);

            GridLayout playerList = findViewById(R.id.gl_playerlist);
            View cardViewOfPlayer = playerList.findViewWithTag(currPlayer);

            if(cardViewOfPlayer instanceof CardView){
                View textViewOfNrOfCards = cardViewOfPlayer.findViewById(R.id.tv_nr_of_cards);
                if(textViewOfNrOfCards instanceof TextView){
                    if(numOfCurrplayerCards != null) {
                        ((TextView) textViewOfNrOfCards).setText(numOfCurrplayerCards.toString());
                    }
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

    @Override
    public void onSendPlayParameters(JSONObject playParameters) {
        JSONObject wishColorRule;

        wishColorRule = playParameters.optJSONObject("Wish Color");

        String wishedColor;

        if (wishColorRule != null){
            wishedColor = wishColorRule.optString("wishForColor");
        } else {
            wishedColor = null;
        }

        if(wishedColor != null){
            if(isValidColor(wishedColor)){
                frameColor = wishedColor.toUpperCase();
            } else {
                frameColor = "NONE";
            }
        } else {
            frameColor = "NONE";
        }
    }
}
