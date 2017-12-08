package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.widget.GridLayout;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.common.collect.Ordering;

import ch.ethz.inf.vs.a4.minker.einz.EinzConstants;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsMessageBody;
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
    private LayoutInflater inflater;
    private EinzClient ourClient;
    private int cardHeight,cardWidth;

    ArrayList<Integer> cardDrawables;
    ArrayList<Card> cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        trayStack = findViewById(R.id.tray_stack);
        trayStack.setOnDragListener(new TrayDragListener());
        mGrid = findViewById(R.id.grid_layout);
        mGrid.setOnDragListener(new DragListener());

        //<UglyHack>
        //this.ourClient = EinzConstants.ourClientGlobal; // DANGER ZONE
        //EinzConstants.ourClientGlobalLck.unlock();
        //</UglyHack>

        inflater = LayoutInflater.from(this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);

        cardWidth  = (size.x / mGrid.getColumnCount());
        cardHeight = (size.y / (3*mGrid.getRowCount()));
        initCards();

        //add cardDrawables to mgrid
        for (int i = 0; i < cardDrawables.size(); i++) {
            final View itemView = inflater.inflate(R.layout.card_view, mGrid, false);
            ImageView localImgView = (ImageView) itemView;
            localImgView.setImageResource(cardDrawables.get(i));
            localImgView.getLayoutParams().width  = cardWidth;
            localImgView.getLayoutParams().height = cardHeight;

            itemView.setOnTouchListener(new LongPressListener());
            mGrid.addView(itemView);
        }
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
        itemView.setOnTouchListener(new LongPressListener());
        mGrid.addView(itemView);
    }

    private void initCards(){
        cardDrawables = new ArrayList<>();
        cards = new ArrayList<>();

        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));
        addCard(new Card("clemens", "bluecard", CardText.ONE, CardColor.BLUE, "drawable", "card_einz_blue"));

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
        //message.getBody().getCards();

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
    public void onCustomActionResponse(EinzMessage<EinzCustomActionMessageBody> message) {

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

    class LongPressListener implements View.OnTouchListener {

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

    class DragListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final View view = (View) event.getLocalState();
            if (view != null && view instanceof ImageView) {

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
                shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y + shadowSize.y / 2);
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
            if (view != null && view instanceof ImageView) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_LOCATION:
                        // do nothing if hovering above own position
                        if (view == v) return true;
                        // get the new list index
                        break;
                    case DragEvent.ACTION_DROP:
                        ImageView tmpView = (ImageView) view;

                        trayStack.setImageDrawable(tmpView.getDrawable());

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
