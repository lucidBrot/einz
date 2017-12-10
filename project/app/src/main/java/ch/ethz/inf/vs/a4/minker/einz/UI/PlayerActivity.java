package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.ClipData;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import ch.ethz.inf.vs.a4.minker.einz.EinzConstants;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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
public class PlayerActivity extends FullscreenActivity { // TODO: onStop and onResume - register this activity at client
    private static final int NBR_ITEMS = 20;
    private GridLayout mGrid;
    private ImageView trayStack;
    private LayoutInflater inflater;
    private EinzClient ourClient;


    int[] cards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        trayStack = findViewById(R.id.tray_stack);
        trayStack.setOnDragListener(new TrayDragListener());
        mGrid = findViewById(R.id.grid_layout);
        mGrid.setOnDragListener(new DragListener());
        initCards();

        //<UglyHack>
        this.ourClient = EinzSingleton.getInstance().getEinzClient();

        inflater = LayoutInflater.from(this);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        for (int i = 0; i < NBR_ITEMS; i++) {
            final View itemView = inflater.inflate(R.layout.card_view, mGrid, false);
            ImageView localImgView = (ImageView) itemView;
            localImgView.setImageResource(cards[i]);
            localImgView.getLayoutParams().width  = (size.x / mGrid.getColumnCount());
            localImgView.getLayoutParams().height = (size.y / (3*mGrid.getRowCount()));
            System.out.println("TESTESTOUT" + mGrid.getMeasuredHeight());
            itemView.setOnTouchListener(new LongPressListener());
            mGrid.addView(itemView);
        }
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


    private void initCards(){
        cards = new int[20];
        cards[0] = R.drawable.card_einz_blue;
        cards[1] = R.drawable.card_take2_red;
        cards[2] = R.drawable.card_take4;
        cards[3] = R.drawable.card_rev_green;
        cards[4] = R.drawable.card_4_red;
        cards[5] = R.drawable.card_2_yellow;
        cards[6] = R.drawable.card_skip_red;
        cards[7] = R.drawable.card_7_green;
        cards[8] = R.drawable.card_2_red;
        cards[9] = R.drawable.card_8_blue;
        cards[10] = R.drawable.card_einz_blue;
        cards[11] = R.drawable.card_take2_red;
        cards[12] = R.drawable.card_take4;
        cards[13] = R.drawable.card_rev_green;
        cards[14] = R.drawable.card_4_red;
        cards[15] = R.drawable.card_2_yellow;
        cards[16] = R.drawable.card_skip_red;
        cards[17] = R.drawable.card_7_green;
        cards[18] = R.drawable.card_2_red;
        cards[19] = R.drawable.card_8_blue;
    }
}
