package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.widget.GridLayout;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzGameOverMessageBody;
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


// How to get Messages:
// get the intent extra that is a reference to ourClient
// make PlayerActivity implement GameUIInterface
// call ourClient.getClientActionCallbackInterface().setGameUI(this)
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
    private EinzClient ourClient;
    private int cardHeight,cardWidth;

    ArrayList<Integer> cardDrawables = new ArrayList<>();
    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<String> availableActions = new ArrayList<>();
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

        drawPile = findViewById(R.id.draw_pile);
        drawPile.setOnTouchListener(new DrawCardListener());
        drawPile.setTag("drawCard");

        mGrid = findViewById(R.id.grid_layout);
        mGrid.setOnDragListener(new HandDragListener());


        this.ourClient = EinzSingleton.getInstance().getEinzClient();

        inflater = LayoutInflater.from(this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        cardWidth  = (size.x / mGrid.getColumnCount());
        cardHeight = (size.y / (3*mGrid.getRowCount()));
        initCards();

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

        localImgView.setTag(cardAdded);

        localImgView.setImageResource(cardAdded.getImageRessourceID(getApplicationContext()));
        localImgView.getLayoutParams().width  = cardWidth;
        localImgView.getLayoutParams().height = cardHeight;
        itemView.setOnTouchListener(new DragCardListener());
        mGrid.addView(itemView);
    }

    private void setTopPlayPileCard(Card cardPlaced){
        trayStack.setImageResource(cardPlaced.getImageRessourceID(getApplicationContext()));
    }

    private void initCards(){
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_2_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_red"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_yellow"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_3_green"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_take4"));

        /*
        cardDrawables.add(R.drawable.card_einz_blue);
        cardDrawables.add(R.drawable.card_take2_red);
        cardDrawables.add(R.drawable.card_take4);
        cardDrawables.add(R.drawable.card_rev_green);
        cardDrawables.add(R.drawable.card_4_red);
        cardDrawables.add(R.drawable.card_2_yellow);
        cardDrawables.add(R.drawable.card_skip_red);
        cardDrawables.add(R.drawable.card_7_green);
        cardDrawables.add(R.drawable.card_2_red);
        cardDrawables.add(R.drawable.card_8_blue);
        cardDrawables.add(R.drawable.card_einz_blue);
        cardDrawables.add(R.drawable.card_take2_red);
        cardDrawables.add(R.drawable.card_take4);
        cardDrawables.add(R.drawable.card_rev_green);
        cardDrawables.add(R.drawable.card_4_red);
        cardDrawables.add(R.drawable.card_2_yellow);
        cardDrawables.add(R.drawable.card_skip_red);
        cardDrawables.add(R.drawable.card_7_green);
        cardDrawables.add(R.drawable.card_2_red);
        cardDrawables.add(R.drawable.card_8_blue);*/
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
        //TODO Update UI for Playerlist so active player is visible
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

                        setTopPlayPileCard((Card)tmpView.getTag());
                        //remove card from inner cardlist

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
