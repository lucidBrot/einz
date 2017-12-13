package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.animation.Animator;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.transition.Explode;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
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
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static java.lang.Thread.sleep;


// How to get Messages:
// get the intent extra that is a reference to ourClient
// make PlayerActivity implement GameUIInterface
// call ourClient.getActionCallbackInterface().setGameUI(this)
// ...
// profit
// Now the client will - so clemens will - call you on these events

// How to send Messages:
// ourClient.getConnection().sendMessage() should do

/**
 * putExtra requires a parcelable, so instead pass the client via Globals
 */
public class PlayerActivity extends FullscreenActivity implements GameUIInterface { // TODO: onStop and onResume - register this activity at client
    private static final int MAXCARDINHAND = 24;
    private GridLayout mGrid;
    private ArrayList<Card> cardStack = new ArrayList<>();
    private ImageView trayStack,trayStack2;

    private String currentlyActivePlayer = "~";

    private ImageView drawPile;
    private LayoutInflater inflater;
    private final double cardSizeRatio = 351.0/251.0;
    private boolean canDrawCard,canEndTurn;
    private Card lastPlayedCard = null;
    private Card seconLastPlayedCard = null;

    private EinzClient ourClient;
    private GridLayout mGridScrollable;


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

    private int cardHeight,cardWidth;

    ArrayList<Integer> cardDrawables = new ArrayList<>();
    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<String> availableActions = new ArrayList<>();
    ArrayList<String> allPlayers = new ArrayList<>();
    String colorChosen = "none";
    LinearLayout llHand;
    ScrollView svHand;

    private HandlerThread backgroundThread = new HandlerThread("NetworkingPlayerActivity");
    private Looper backgroundLooper;
    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_player);

        this.backgroundThread.start();
        this.backgroundLooper = this.backgroundThread.getLooper();
        this.backgroundHandler = new Handler(this.backgroundLooper);

        trayStack = findViewById(R.id.tray_stack);
        trayStack.setOnDragListener(new TrayDragListener());

        trayStack2 = findViewById(R.id.tray_stack_2);

        mGrid = findViewById(R.id.grid_layout);
        mGridScrollable = findViewById(R.id.grid_layout_scrollable);

        svHand = findViewById(R.id.sv_hand_scrollable);
        svHand.setOnDragListener(new HandDragListenerScrollable());

        llHand = findViewById(R.id.ll_hand);
        llHand.setOnDragListener(new HandDragListener());

        drawPile = findViewById(R.id.draw_pile);
        drawPile.setOnTouchListener(new DrawCardListener());
        drawPile.setTag("drawCard");

        //mGrid.setOnDragListener(new HandDragListener());

        Button colorWheelBlueButton = findViewById(R.id.btn_colorwheel_blue);
        colorWheelBlueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onColorWheelButtonBlueClick();
            }
        });

        Button colorWheelYellowButton = findViewById(R.id.btn_colorwheel_yellow);
        colorWheelYellowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onColorWheelButtonYellowClick();
            }
        });

        Button colorWheelRedButton = findViewById(R.id.btn_colorwheel_red);
        colorWheelRedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onColorWheelButtonRedClick();
            }
        });

        Button colorWheelGreenButton = findViewById(R.id.btn_colorwheel_green);
        colorWheelGreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onColorWheelButtonGreenClick();
            }
        });

        Button endTurnButton = findViewById(R.id.btn_end_turn);
        endTurnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTurn();
            }
        });

        Button endGame = findViewById(R.id.btn_end_game);
        endGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBackToMainMenu();
            }
        });

        inflater = LayoutInflater.from(this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        cardWidth  = (size.x / 8);
        cardHeight = (size.y /(12));
        //initCards();

        //add cardDrawables to mgrid
        /*
        for (int i = 0; i < cardDrawables.size(); i++) {
            final View itemView = inflater.inflate(R.layout.card_view, mGrid, false);
            ImageView localImgView = (ImageView) itemView;
            localImgView.setImageResource(cardDrawables.get(i));
            localImgView.getLayoutParams().width  = cardWidth;
            localImgView.getLayoutParams().height = cardHeight;

            itemView.setOnTouchListener(new DragCardListener());
            mGrid.addView(itemView);
        }*/
        this.ourClient = EinzSingleton.getInstance().getEinzClient();
        ourClient.getActionCallbackInterface().setGameUI(this);
    }

    private int calculateNewIndex(GridLayout gl,float x, float y) {
        // calculate which column to move to
        final float cellWidth = gl.getWidth() / gl.getColumnCount();
        final int column = (int)(x / cellWidth);

        // calculate which row to move to
        final float cellHeight = gl.getHeight() / gl.getRowCount();
        final int row = (int)Math.floor(y / cellHeight);

        // the items in the GridLayout is organized as a wrapping list
        // and not as an actual grid, so this is how to get the new index
        int index = row * gl.getColumnCount() + column;
        if (index >= gl.getChildCount()) {
            index = gl.getChildCount() - 1;
        }

        return index;
    }

    private void addCard(Card cardAdded){
        cards.add(cardAdded);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        //add cardDrawables to mgrid
        final View itemView = inflater.inflate(R.layout.card_view, mGrid, false);
        ImageView localImgView = (ImageView) itemView;

        final View itemView2 = inflater.inflate(R.layout.card_view, mGridScrollable, false);
        ImageView localImgView2 = (ImageView) itemView2;

        // added this temporary fix for the OOM error problem
        // TODO: add permanent fix
        // https://stackoverflow.com/a/13415604/2550406

        localImgView.setTag(cardAdded);
        localImgView2.setTag(cardAdded);

        //((BitmapDrawable)localImgView.getDrawable()).getBitmap().recycle();

        //localImgView.setImageResource(cardAdded.getImageRessourceID(getApplicationContext())); // TODO: @Chris fix OOM error. Seems to happen at the 12th addcard
        localImgView.getLayoutParams().width  = cardWidth;
        localImgView.getLayoutParams().height = cardHeight;

        localImgView2.getLayoutParams().width  = cardWidth;
        localImgView2.getLayoutParams().height = cardHeight;

        Bitmap b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            b = ((BitmapDrawable)getResources().getDrawable(cardAdded.getImageRessourceID(getApplicationContext()), getApplicationContext().getTheme())).getBitmap();
        } else {
            b = ((BitmapDrawable)getResources().getDrawable(cardAdded.getImageRessourceID(getApplicationContext()))).getBitmap();
        }

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, (int)(1.0/cardSizeRatio * (double)cardHeight),cardHeight, false);

        localImgView.setImageBitmap(bitmapResized);
        localImgView2.setImageBitmap(bitmapResized);

        itemView.setOnTouchListener(new DragCardListener());

        itemView2.setOnTouchListener(new DragCardListener());

        mGrid.addView(itemView);
        mGridScrollable.addView(itemView2);
    }

    private void rearrangeHandTooBig(){
        svHand.setVisibility(View.VISIBLE);
        llHand.setVisibility(View.GONE);
    }

    private void rearrangeHandSmallEnough(){
        svHand.setVisibility(View.GONE);
        llHand.setVisibility(View.VISIBLE);
    }

    private void removeCardFromHand(Card cardRemoved){
        if(cardRemoved != null) {
            int numberOfCardsBefore = cards.size();
            cards.remove(cardRemoved);
            mGrid.removeView(mGrid.findViewWithTag(cardRemoved));
            mGridScrollable.removeView(mGridScrollable.findViewWithTag(cardRemoved));
            if(numberOfCardsBefore > MAXCARDINHAND && cards.size() <= MAXCARDINHAND){
                rearrangeHandSmallEnough();
            }
        }
    }

    private void setlastplayedCard(Card lastplayedCard){
        this.lastPlayedCard = lastplayedCard;
    }

    public void setTopPlayedCard(Card cardToSet) {

        //((BitmapDrawable)trayStack.getDrawable()).getBitmap().recycle();
        if(trayStack.getWidth()<=0||trayStack.getHeight()<=0){
            Log.w("PlayerActivity/setTopPlayedCard", "using sleep hack because trayStack had height or width 0 or less");
            try {
                sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Bitmap b = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()), getApplicationContext().getTheme())).getBitmap();
        } else {
            b = ((BitmapDrawable)getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()))).getBitmap();
        }

        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, trayStack.getWidth(),(int)(cardSizeRatio * (double)trayStack.getWidth()), false);
        trayStack.setImageBitmap(bitmapResized);

        //if()
        double direction = Math.random() * 2*Math.PI;
        double xTranslation = Math.cos(direction) * 1500;
        double yTranslation = Math.sin(direction) * 1500;

        trayStack.setVisibility(View.VISIBLE);
        trayStack.animate().translationX((int)xTranslation).translationY((int)yTranslation).setDuration(0).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
            @Override
            public void run() {
                trayStack.setVisibility(View.VISIBLE);
                trayStack.animate().translationX(0).translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(500).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        trayStack2.setImageBitmap(bitmapResized);
                    }
                });

            }
        });


    }

    private void initCards(){
        // here's how you would do that
        CardLoader cardLoader = EinzSingleton.getInstance().getCardLoader();
        addCard(cardLoader.getCardInstance("yellow_1"));
        // </education>
        /*
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_take4"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_take4"));*/
    }

    public boolean checkCardsStillValid(ArrayList<Card> cardlist){
        return checkCardListIdentical(cardlist,cards);
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

    public void playCard(final Card playedCard){
        this.backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ourClient.getConnection().sendMessage(
                            "{\"header\":{\"messagegroup\":\"playcard\",\"messagetype\":\"PlayCard\"},\"body\":{\"card\":{\"ID\":\"" + playedCard.getID() + "\",\"origin\":\"" + playedCard.getOrigin() + "\"}}}");
                } catch (SendMessageFailureException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void drawCard(){
        this.backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ourClient.getConnection().sendMessage("{\"header\":{\"messagegroup\":\"draw\",\"messagetype\":\"DrawCards\"},\"body\":{}}");
                } catch (SendMessageFailureException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void endTurn(){
        this.backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ourClient.getConnection().sendMessage("{\"header\":{\"messagegroup\":\"furtheractions\",\"messagetype\":\"FinishTurn\"},\"body\":{}}");
                } catch (SendMessageFailureException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void clearHand(){
        mGrid.removeAllViews();
        cards.clear();
        cardDrawables.clear();
    }

    public void addToHand(ArrayList<Card> addedCards){
        int nrOfCardsBefore = cards.size();
        for (Card currCard:addedCards){
            int numberOfCardsBefore = cards.size();
            addCard(currCard);
            if(numberOfCardsBefore <= MAXCARDINHAND && cards.size() > MAXCARDINHAND){
                rearrangeHandTooBig();
            }
        }
    }

    public void setAvailableActions(ArrayList<String> actions){
        availableActions = actions;
        //canPlayCard = availableActions.contains("drawCards");
    }

    public void addPlayerToList(String addedPlayer){
        //ensure no player is added twice
        if(!allPlayers.contains(addedPlayer)) {
            LinearLayout playerList = findViewById(R.id.ll_playerlist);

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

    public void displayColorWheel(){
        LinearLayout colorWheel = findViewById(R.id.ll_colorwheel);
        colorWheel.setVisibility(View.VISIBLE);
    }

    public void hideColorWheel(){
        LinearLayout colorWheel = findViewById(R.id.ll_colorwheel);
        colorWheel.setVisibility(View.INVISIBLE);
    }

    public void onColorWheelButtonGreenClick(){
        hideColorWheel();
        colorChosen = "green";
    }

    public void onColorWheelButtonRedClick(){
        hideColorWheel();
        colorChosen = "red";
    }

    public void onColorWheelButtonBlueClick(){
        hideColorWheel();
        colorChosen = "blue";
    }

    public void onColorWheelButtonYellowClick(){
        hideColorWheel();
        colorChosen = "yellow";
    }

    public void removePlayerFromList(String playerToBeRemoved){
        if(allPlayers.contains(playerToBeRemoved)) {
            LinearLayout playerList = findViewById(R.id.ll_playerlist);

            for (int i = 0; i < playerList.getChildCount();) {
                View v = playerList.getChildAt(i);

                if (v instanceof CardView && v.getTag().equals(playerToBeRemoved)){
                       // Do something
                    playerList.removeView(v);
                } else {
                    i++;
                }
            }

            allPlayers.remove(playerToBeRemoved);
        }
    }

    public void setCanDrawCard(boolean canDrawCard) {

        this.canDrawCard = canDrawCard;

        if(canDrawCard){
            drawPile = findViewById(R.id.draw_pile);
            drawPile.setOnTouchListener(new DrawCardListener());
        } else {
            drawPile = findViewById(R.id.draw_pile);
            drawPile.setOnTouchListener(null);
        }
    }

    public void setCanEndTurn(boolean canEndTurn) {
        this.canEndTurn = canEndTurn;
    }

    public void goBackToMainMenu(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message) {

    }

    @Override
    public void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message) {

    }

    @Override
    public void onShowToast(EinzMessage<EinzShowToastMessageBody> message) {
        Context context = getApplicationContext();
        CharSequence text = message.getBody().getToast();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onKickFailure(EinzMessage<EinzKickFailureMessageBody> message) {

    }

    @Override
    public void onDrawCardsSuccess(EinzMessage<EinzDrawCardsSuccessMessageBody> message) {
        addToHand(message.getBody().getCards());
    }

    @Override
    public void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message) {

    }

    @Override
    public void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message) {
        if(message.getBody().getSuccess().equals("true")){
            setTopPlayedCard(lastPlayedCard);
            if(lastPlayedCard != null){
                removeCardFromHand(lastPlayedCard);
            }
        }
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
        LinearLayout winningPlayers = findViewById(R.id.ll_winning_players);
        HashMap<String,String> playerPoints = message.getBody().getPoints();

        for(String player:allPlayers){

            CardView usercard = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_playerpointlist_element, winningPlayers, false);
                // false because don't add view yet - I first want to set some text

            TextView tv_username = usercard.findViewById(R.id.tv_playerlist_username);
            TextView tv_points = usercard.findViewById(R.id.tv_nr_of_points);

                // set text
            tv_username.setText(player);
            tv_points.setText(String.valueOf(playerPoints.get(player)));

                // add view
            winningPlayers.addView(usercard);
        }
    }

    @Override
    public void setHand(ArrayList<Card> hand) {
        Log.w("PlayerActivity", "setHand is currently enabled. This means that the cards for debugging will not be shown.");
        // to disable, just comment out the following four lines
        int numberOfCardsBefore = cards.size();
        if(!checkCardsStillValid(hand)){
            clearHand();
            addToHand(hand);
            if(numberOfCardsBefore > MAXCARDINHAND && cards.size() <= MAXCARDINHAND){
                rearrangeHandSmallEnough();
            } else if (numberOfCardsBefore <= MAXCARDINHAND && cards.size() > MAXCARDINHAND) {
                rearrangeHandTooBig();
            }
        }
    }

    @Override
    public void setActions(ArrayList<String> actions) {
        setAvailableActions(actions);
    }



    @Override
    public void playerStartedTurn(String playerThatStartedTurn) {

        currentlyActivePlayer = playerThatStartedTurn;

        if (playerThatStartedTurn.equals(ourClient.getUsername())) {

            setCanDrawCard(true);

            Context context = getApplicationContext();
            CharSequence text = "It's your turn " + ourClient.getUsername();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        if(allPlayers.contains(playerThatStartedTurn)) {
            LinearLayout playerList = findViewById(R.id.ll_playerlist);

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
            LinearLayout playerList = findViewById(R.id.ll_playerlist);
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
        // TODO: change display of players when somebody left.
    }

    @Override
    public void setStack(ArrayList<Card> stack) {
        if(!checkCardListIdentical(stack,cardStack)) {
            Card sndCard = null;

            final Card topCard = stack.get(stack.size() - 1);

            setTopPlayedCard(topCard);

            cardStack = stack;
        }
    }

    @Override
    public void onKeepaliveTimeout() {
        // TODO: what to do when lost connection? probably return to main menu
    }

    class DragCardListener implements View.OnTouchListener {

        /*@Override
        public boolean onLongClick(View view) {
            final ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                view.startDragAndDrop(data, shadowBuilder, view, 0);
            } else {
                view.startDrag(data, shadowBuilder, view, 0);
            }
            view.setVisibility(View.INVISIBLE);
            return true;
        }*/


        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                final ClipData data = ClipData.newPlainText("", "");

                View.DragShadowBuilder shadowBuilder = new CustomDragShadowBuilder(view);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, view, 0);
                } else {
                    view.startDrag(data, shadowBuilder, view, 0);
                }
                return true;
            }

            return false;
        }
    }

    class DrawCardListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if (motionEvent.getAction()==MotionEvent.ACTION_DOWN) {
                final ClipData data = ClipData.newPlainText("", "");

                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    view.startDragAndDrop(data, shadowBuilder, view, 0);
                } else {
                    view.startDrag(data, shadowBuilder, view, 0);
                }
                return true;
            }


            return false;
        }
    }

    class HandDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();
            if (view != null && view instanceof ImageView && view.getTag() instanceof Card) {

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        view.setVisibility(View.INVISIBLE);
                    case DragEvent.ACTION_DRAG_LOCATION:
                        // do nothing if hovering above own position
                        if (view == v) return true;
                        // get the new list index
                        final int index = calculateNewIndex(mGrid,event.getX(), event.getY());
                        // remove the view from the old position
                        mGrid.removeView(view);
                        // and push to the new
                        mGrid.addView(view, index);

                        break;
                    case DragEvent.ACTION_DROP:
                        view.setVisibility(View.VISIBLE);

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        view.setVisibility(View.VISIBLE);

                        break;
                    default:
                        break;
                }
                return true;

            } else if (view != null && view instanceof ImageView && view.getTag().equals("drawCard")) {
                switch (event.getAction()) {

                    case DragEvent.ACTION_DROP:
                        drawCard();
                        System.out.println("drew card");

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:

                        break;
                    default:
                        break;
                }
                return true;
            }
            return false;
        }
    }

    class HandDragListenerScrollable implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();
            if (view != null && view instanceof ImageView && view.getTag() instanceof Card) {

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        view.setVisibility(View.INVISIBLE);
                    case DragEvent.ACTION_DRAG_LOCATION:
                        // do nothing if hovering above own position
                        if (view == v) return true;
                        // get the new list index
                        final int index = calculateNewIndex(mGridScrollable,event.getX(), event.getY());
                        // remove the view from the old position
                        mGridScrollable.removeView(view);
                        // and push to the new
                        mGridScrollable.addView(view, index);

                        break;
                    case DragEvent.ACTION_DROP:
                        view.setVisibility(View.VISIBLE);

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        view.setVisibility(View.VISIBLE);

                        break;
                    default:
                        break;
                }
                return true;

            } else if (view != null && view instanceof ImageView && view.getTag().equals("drawCard")) {
                switch (event.getAction()) {

                    case DragEvent.ACTION_DROP:
                        drawCard();
                        System.out.println("drew card");

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:

                        break;
                    default:
                        break;
                }
                return true;
            }
            return false;
        }
    }

    class CustomDragShadowBuilder extends View.DragShadowBuilder{
        CustomDragShadowBuilder(View v){
            super(v);
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            final View view = getView();
            if (view != null) {
                shadowSize.set(view.getWidth()*2, view.getHeight()*2);
                shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y);
            }
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            final View view = getView();
            if (view != null) {
                canvas.scale(2,2);
                canvas.translate(0,0);
                view.draw(canvas);
            }
        }
    }

    class TrayDragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();
            if (view != null && view instanceof ImageView && view.getTag() instanceof Card) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_LOCATION:
                        // do nothing if hovering above own position
                        if (view == v) return true;
                        // get the new list index
                        break;
                    case DragEvent.ACTION_DROP:
                        ImageView tmpView = (ImageView) view;

                        setlastplayedCard((Card)tmpView.getTag());
                        //remove card from inner cardlist
                         playCard((Card)tmpView.getTag());

                        //cards.remove((Card) tmpView.getTag());
                        //System.out.println(cards);

                        //remove card from View
                        //mGrid.removeView(view);
                        view.setVisibility(View.VISIBLE);


                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        view.setVisibility(View.VISIBLE);

                        break;
                    default:
                        break;
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // TODO: directly stop server and client
        //      currently, back only takes the user to the LobbyActivity, where they have to press back again.
    }
}
