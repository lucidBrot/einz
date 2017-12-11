package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.ClipData;
import android.content.Context;
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
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardOrigin;
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
    private static final int NBR_ITEMS = 20;
    private GridLayout mGrid;
    private ImageView trayStack;
    private ImageView drawPile;
    private LayoutInflater inflater;

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

    private EinzClient ourClient;
    private int cardHeight,cardWidth;

    ArrayList<Integer> cardDrawables = new ArrayList<>();
    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<String> availableActions = new ArrayList<>();
    ArrayList<String> allPlayers = new ArrayList<>();
    String colorChosen = "none";

    private HandlerThread backgroundThread = new HandlerThread("NetworkingPlayerActivity");
    private Looper backgroundLooper;
    private Handler backgroundHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_player);

        this.ourClient = EinzSingleton.getInstance().getEinzClient();
        ourClient.getActionCallbackInterface().setGameUI(this);

        this.backgroundThread.start();
        this.backgroundLooper = this.backgroundThread.getLooper();
        this.backgroundHandler = new Handler(this.backgroundLooper);

        trayStack = findViewById(R.id.tray_stack);
        trayStack.setOnDragListener(new TrayDragListener());

        drawPile = findViewById(R.id.draw_pile);
        drawPile.setOnTouchListener(new DrawCardListener());
        drawPile.setTag("drawCard");

        mGrid = findViewById(R.id.grid_layout);
        mGrid.setOnDragListener(new HandDragListener());

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

        inflater = LayoutInflater.from(this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        cardWidth  = (size.x / mGrid.getColumnCount());
        cardHeight = (size.y / (3*mGrid.getRowCount()));
        initCards();

        addPlayerToList("silvia");
        addPlayerToList("josua");
        addPlayerToList("clemens");
        addPlayerToList("chris");
        addPlayerToList("fabian");
        addPlayerToList("eric");
        addPlayerToList("mr.iamsounbelievable");

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
    }

    private int calculateNewIndex(float x, float y) {
        // calculate which column to move to
        final float cellWidth = mGrid.getWidth() / mGrid.getColumnCount();
        final int column = (int)(x / cellWidth);

        // calculate which row to move to
        final float cellHeight = mGrid.getHeight() / mGrid.getRowCount();
        final int row = (int)Math.floor(y / cellHeight);

        // the items in the GridLayout is organized as a wrapping list
        // and not as an actual grid, so this is how to get the new index
        int index = row * mGrid.getColumnCount() + column;
        if (index >= mGrid.getChildCount()) {
            index = mGrid.getChildCount() - 1;
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

        // added this temporary fix for the OOM error problem
        // TODO: add permanent fix
        // https://stackoverflow.com/a/13415604/2550406

        localImgView.setTag(cardAdded);

        localImgView.setImageResource(cardAdded.getImageRessourceID(getApplicationContext())); // TODO: @Chris fix OOM error. Seems to happen at the 12th addcard
        localImgView.getLayoutParams().width  = cardWidth;
        localImgView.getLayoutParams().height = cardHeight;
        itemView.setOnTouchListener(new DragCardListener());
        mGrid.addView(itemView);
    }

    private void setTopPlayPileCard(Card cardPlaced){
        trayStack.setImageResource(cardPlaced.getImageRessourceID(getApplicationContext()));
    }

    private void initCards(){
        // here's how you would do that
        CardLoader cardLoader = EinzSingleton.getInstance().getCardLoader();
        addCard(cardLoader.getCardInstance("yellow_1"));
        // </education>

        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_1_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_blue"));
        /*addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_take4"));*/
    }

    public boolean checkCardsStillValid(ArrayList<Card> cardlist){
        if(cardlist.size() != cards.size()){
            return false;
        }

        ArrayList<String> stringOfOwnCards = new ArrayList<>();
        ArrayList<String> stringOfGotCards = new ArrayList<>();

        for(Card currCard:cards){
            stringOfOwnCards.add(currCard.getID());
        }

        for(Card currCard:cardlist){
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
        for (Card currCard:addedCards){
            addCard(currCard);
        }
    }

    public void setAvailableActions(ArrayList<String> actions){
        availableActions = actions;
    }

    public void addPlayerToList(String addedPlayer){
        //ensure no player is added twice
        if(!allPlayers.contains(addedPlayer)) {
            LinearLayout playerList = findViewById(R.id.ll_playerlist);

            CardView usercard = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_playerlist, playerList, false);
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
        colorWheel.setVisibility(View.GONE);
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
        Log.w("PlayerActivity", "setHand is currently enabled. This means that the cards for debugging will not be shown.");
        // to disable, just comment out the following four lines
        if(!checkCardsStillValid(hand)){
            clearHand();
            addToHand(hand);
        }
    }

    @Override
    public void setActions(ArrayList<String> actions) {
        setAvailableActions(actions);
    }

    @Override
    public void playerStartedTurn(String playerThatStartedTurn) {
        if (playerThatStartedTurn.equals(ourClient.getUsername())) {
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
                        final int index = calculateNewIndex(event.getX(), event.getY());
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
                        //drawCard();
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

                        setTopPlayPileCard((Card)tmpView.getTag());
                        //remove card from inner cardlist
                        // playCard((Card)tmpView.getTag());

                        cards.remove((Card) tmpView.getTag());
                        //System.out.println(cards);

                        //remove card from View
                        mGrid.removeView(view);
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
}
