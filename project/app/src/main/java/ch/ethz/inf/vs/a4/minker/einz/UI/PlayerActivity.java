package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;

import org.json.JSONArray;

import org.json.JSONException;
import org.json.JSONObject;


import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzGameOverMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzInitGameMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKickFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayerFinishedMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSendStateMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzShowToastMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;

import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.SelectorRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.WishColorRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public class PlayerActivity extends FullscreenActivity implements GameUIInterface, SelectorFragment.SelectorCallbackInterface {
    private static final int MAXCARDINHAND = 24;
    private GridLayout mGrid, mGridScrollable;
    private ArrayList<Card> cardStack = new ArrayList<>();
    private ImageView trayStack, trayStack2, colorBorder;

    private String currentlyActivePlayer = "~";

    private ImageView drawPile;
    private LayoutInflater inflater;
    private final double cardSizeRatio = 351.0 / 251.0;
    private boolean canDrawCard;
    private boolean canEndTurn;
    private Card lastPlayedCard = null;
    private Card seconLastPlayedCard = null;

    private EinzClient ourClient;
    private HashMap<String, Double> orientationOfPlayer = new HashMap<>();

    private Map<String, List<BasicCardRule>> ruleMapping;
    private String frameColor = "NONE";

    private int cardHeight;
    private int cardWidth;
    private int cardBigWidth;
    private int cardBigHeight;

    ArrayList<Integer> cardDrawables = new ArrayList<>();
    ArrayList<Card> cards = new ArrayList<>();
    ArrayList<String> availableActions = new ArrayList<>();
    ArrayList<String> allPlayers = new ArrayList<>();
    String colorChosen = "none";
    LinearLayout llHand;
    LinearLayout llGame;
    ScrollView svHand;

    private HandlerThread backgroundThread = new HandlerThread("NetworkingPlayerActivity");
    private Looper backgroundLooper;
    private Handler backgroundHandler;

    private SelectorFragment selector;

    private GlobalState currentGlobalState;

    @Override
    protected void onPause() {
        super.onPause();
        if (ourClient != null && ourClient.getActionCallbackInterface() != null)
            ourClient.getActionCallbackInterface().setGameUI(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ourClient != null && ourClient.getActionCallbackInterface() != null)
            ourClient.getActionCallbackInterface().setGameUI(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_player);

        this.backgroundThread.start();
        this.backgroundLooper = this.backgroundThread.getLooper();
        this.backgroundHandler = new Handler(this.backgroundLooper);

        trayStack = findViewById(R.id.tray_stack);
        trayStack.setOnDragListener(new TrayDragListener());

        colorBorder = findViewById(R.id.iv_wished_color);

        trayStack2 = findViewById(R.id.tray_stack_2);

        llGame = findViewById(R.id.ll_game);
        llGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideColorWheel();
            }
        });

        mGrid = findViewById(R.id.grid_layout);
        mGridScrollable = findViewById(R.id.grid_layout_scrollable);

        mGridScrollable.setOnDragListener(new HandDragListenerScrollable());

        svHand = findViewById(R.id.sv_hand_scrollable);
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

        double cardBigWidth_double = (((double) size.x) / 7.) * 3.;
        cardBigWidth = (int) cardBigWidth_double;
        cardBigHeight = (int) ((double) cardBigWidth * cardSizeRatio);
        cardWidth = (size.x / 8);
        cardHeight = (size.y / (12));
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

//        View fragmentView = findViewById(R.id.selector_fragment);
//        fragmentView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnect();
        goBackToMainMenu();
        // TODO: directly stop server and client
        //      currently, back only takes the user to the LobbyActivity, where they have to press back again.
    }

    private void disconnect() {
        this.backgroundHandler.post(new Runnable() {
            @Override
            public void run() {
                EinzUnregisterRequestMessageBody body = new EinzUnregisterRequestMessageBody(ourClient.getUsername());
                EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterRequest");
                try {
                    ourClient.getConnection().sendMessage(new EinzMessage<>(header, body));
                } catch (SendMessageFailureException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int calculateNewIndex(GridLayout gl, float x, float y) {
        // calculate which column to move to
        final float cellWidth = gl.getWidth() / gl.getColumnCount();
        final int column = (int) (x / cellWidth);

        // calculate which row to move to
        final float cellHeight = gl.getHeight() / gl.getRowCount();
        final int row = (int) Math.floor(y / cellHeight);

        // the items in the GridLayout is organized as a wrapping list
        // and not as an actual grid, so this is how to get the new index
        int index = row * gl.getColumnCount() + column;
        if (index >= gl.getChildCount()) {
            index = gl.getChildCount() - 1;
        }

        return index;
    }

    private void addCard(Card cardAdded) {
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
        localImgView.getLayoutParams().width = cardWidth;
        localImgView.getLayoutParams().height = cardHeight;

        localImgView2.getLayoutParams().width = cardWidth;
        localImgView2.getLayoutParams().height = cardHeight;

        Bitmap b;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            b = ((BitmapDrawable) getResources().getDrawable(cardAdded.getImageRessourceID(getApplicationContext()), getApplicationContext().getTheme())).getBitmap();
        } else {
            b = ((BitmapDrawable) getResources().getDrawable(cardAdded.getImageRessourceID(getApplicationContext()))).getBitmap();
        }

        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, (int) (1.0 / cardSizeRatio * (double) cardHeight), cardHeight, false);

        localImgView.setImageBitmap(bitmapResized);
        localImgView2.setImageBitmap(bitmapResized);

        itemView.setOnTouchListener(new DragCardListener());

        itemView2.setOnTouchListener(new DragCardListener());

        mGrid.addView(itemView);
        mGridScrollable.addView(itemView2);
    }

    private void rearrangeHandTooBig() {
        svHand.setVisibility(View.VISIBLE);
        llHand.setVisibility(View.GONE);
    }

    private void rearrangeHandSmallEnough() {
        llHand.setVisibility(View.VISIBLE);
        svHand.setVisibility(View.GONE);
    }

    private void removeCardFromHand(Card cardRemoved) {
        if (cardRemoved != null) {
            if (!(mGridScrollable.getChildCount() == mGrid.getChildCount() && mGrid.getChildCount() == cards.size())) {
                System.out.println("will fail to remove card");
            }

            mGridScrollable.removeView(mGridScrollable.findViewWithTag(cardRemoved));
            mGrid.removeView(mGrid.findViewWithTag(cardRemoved));

            cards.remove(cardRemoved);

            if (cards.size() + 1 > MAXCARDINHAND && cards.size() <= MAXCARDINHAND) {
                rearrangeHandSmallEnough();
            }
        }
    }

    private void setlastplayedCard(Card lastplayedCard) {
        this.lastPlayedCard = lastplayedCard;
    }

    public void setTopPlayedCard(Card cardToSet) {

        //((BitmapDrawable)trayStack.getDrawable()).getBitmap().recycle();

        Bitmap b = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            b = ((BitmapDrawable) getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()), getApplicationContext().getTheme())).getBitmap();
        } else {
            b = ((BitmapDrawable) getResources().getDrawable(cardToSet.getImageRessourceID(getApplicationContext()))).getBitmap();
        }

        final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, cardBigWidth, cardBigHeight, false);
        trayStack.setImageBitmap(bitmapResized);

        double direction;

        if (orientationOfPlayer.containsKey(currentlyActivePlayer) && !Double.isNaN(orientationOfPlayer.get(currentlyActivePlayer))) {
            if (currentlyActivePlayer.equals(ourClient.getUsername())) {
                direction = -3.0 / 2.0 * Math.PI;
            } else {
                direction = orientationOfPlayer.get(currentlyActivePlayer);
            }

        } else {
            direction = Math.random() * 2 * Math.PI;
        }

        double xTranslation = Math.cos(direction) * 1500;
        double yTranslation = Math.sin(direction) * 1500;

        trayStack.setVisibility(View.GONE);
        colorBorder.setVisibility(View.GONE);

        trayStack.animate().translationX((int) xTranslation).translationY((int) yTranslation).setDuration(0).setInterpolator(new AccelerateDecelerateInterpolator()).withEndAction(new Runnable() {
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
        switch (frameColor) {
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

    private void initCards() {
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

    public boolean checkCardsStillValid(ArrayList<Card> cardlist) {
        return checkCardListIdentical(cardlist, cards);
    }

    public boolean checkCardListIdentical(ArrayList<Card> cardlist1, ArrayList<Card> cardlist2) {
        if (cardlist1.size() != cardlist2.size()) {
            return false;
        }

        ArrayList<String> stringOfOwnCards = new ArrayList<>();
        ArrayList<String> stringOfGotCards = new ArrayList<>();

        for (Card currCard : cardlist2) {
            stringOfOwnCards.add(currCard.getID());
        }

        for (Card currCard : cardlist1) {
            stringOfGotCards.add(currCard.getID());
        }

        Collections.sort(stringOfGotCards);
        Collections.sort(stringOfOwnCards);

        return (stringOfGotCards.equals(stringOfOwnCards));
    }

    public void playCard(final Card playedCard) {
        hideColorWheel();

        setlastplayedCard(playedCard);

        if (isWishingCard(playedCard)) {
            displayColorWheel();
        } else if (isSelectorCard(playedCard)) {
            for (BasicCardRule rule : ruleMapping.get(playedCard.getID())) {
                if (rule instanceof SelectorRule) {
                    SelectorRule selectorRule = ((SelectorRule) rule);
                    showSelector(selectorRule.getSelectionTitle(), selectorRule.getChoices(currentGlobalState), rule.getName());
                }
            }
        } else {
            this.backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        EinzPlayCardMessageBody body = new EinzPlayCardMessageBody(lastPlayedCard);
                        EinzMessageHeader header = new EinzMessageHeader("playcard", "PlayCard");
                        ourClient.getConnection().sendMessage(new EinzMessage<>(header, body));
                    } catch (SendMessageFailureException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    private boolean isWishingCard(Card playedCard) {
        List<BasicCardRule> rules = ruleMapping.get(playedCard.getID());
        if (rules == null) {
            return false;
        }

        for (BasicCardRule rule : rules) {
            if (rule instanceof WishColorRule) {
                return true;
            }
        }
        return false;
    }

    private boolean isSelectorCard(Card played) {
        List<BasicCardRule> rules = ruleMapping.get(played.getID());
        if (rules == null) {
            return false;
        }

        for (BasicCardRule rule : rules) {
            if (rule instanceof SelectorRule) {
                return true;
            }
        }
        return false;
    }

    public void displayColorWheel() {
        LinearLayout colorWheel = findViewById(R.id.ll_colorwheel);
        colorWheel.setVisibility(View.VISIBLE);
    }

    public void hideColorWheel() {
        LinearLayout colorWheel = findViewById(R.id.ll_colorwheel);
        colorWheel.setVisibility(View.INVISIBLE);
    }

    public void onColorWheelButtonGreenClick() {
        hideColorWheel();
        final String chosenColor = "green";
        colorChosen = chosenColor;
        if (isWishingCard(lastPlayedCard)) {
            onItemSelected(chosenColor, "Wish Color");
        }
    }

    public void onColorWheelButtonRedClick() {
        hideColorWheel();
        final String chosenColor = "red";
        colorChosen = chosenColor;
        if (isWishingCard(lastPlayedCard)) {
            onItemSelected(chosenColor, "Wish Color");
        }
    }

    public void onColorWheelButtonBlueClick() {
        hideColorWheel();
        final String chosenColor = "blue";
        colorChosen = chosenColor;
        if (isWishingCard(lastPlayedCard)) {
            onItemSelected(chosenColor, "Wish Color");
        }
    }

    public void onColorWheelButtonYellowClick() {
        hideColorWheel();
        final String chosenColor = "yellow";
        colorChosen = chosenColor;
        if (isWishingCard(lastPlayedCard)) {
            onItemSelected(chosenColor, "Wish Color");
        }
    }
//    @Deprecated
//    private void colorWheelButtonSendMessage(final String chosenColor){
//        this.backgroundHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    JSONObject playParameters;
//                    try {
//                        playParameters = new JSONObject("{\"Wish Color\":{\"wishForColor\":\"" + chosenColor + "\"}}");
//                        EinzPlayCardMessageBody body = new EinzPlayCardMessageBody(lastPlayedCard,playParameters);
//                        EinzMessageHeader header = new EinzMessageHeader("playcard","PlayCard");
//                        ourClient.getConnection().sendMessage(new EinzMessage<>(header,body));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                } catch (SendMessageFailureException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    public void onItemSelected(final String selection, final String ruleName) {
        hideSelector();
        Log.d("SelectorCallback", "Got selectorCallback \"" + selection + "\"");

        List<BasicCardRule> rules = ruleMapping.get(lastPlayedCard.getID());
        SelectorRule selectorRule = null;
        for (BasicCardRule rule : rules) {
            if (rule.getName().equals(ruleName)) {
                selectorRule = (SelectorRule) rule;
            }
        }

        if (selectorRule != null) {
            final SelectorRule finalRule = selectorRule;
            this.backgroundHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject playParameters;
                        try {
                            playParameters = new JSONObject();
                            JSONObject ruleParameter = finalRule.makeSelectionReadyForSend(selection);
                            playParameters.put(ruleName, ruleParameter);
                            EinzPlayCardMessageBody body = new EinzPlayCardMessageBody(lastPlayedCard, playParameters);
                            EinzMessageHeader header = new EinzMessageHeader("playcard", "PlayCard");
                            ourClient.getConnection().sendMessage(new EinzMessage<>(header, body));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (SendMessageFailureException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private void showSelector(String title, Map<String, String> choices, String ruleName) {
        ArrayList<String> values = new ArrayList<>();

        ArrayList<String> text = new ArrayList<>();
        for (String key : choices.keySet()) {
            values.add(key);
            text.add(choices.get(key));
        }

        selector = new SelectorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(SelectorFragment.TITLE_KEY, title);
        arguments.putStringArrayList(SelectorFragment.CHOICE_LIST_TEXT, text);
        arguments.putStringArrayList(SelectorFragment.CHOICE_LIST_VALUES, values);
        arguments.putString(SelectorFragment.RULE_NAME, ruleName);
        selector.setArguments(arguments);

        View fragment = findViewById(R.id.selector_fragment);
        fragment.setVisibility(View.VISIBLE);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.selector_fragment, selector);
        fragmentTransaction.commit();
    }

    private void hideSelector() {
        View fragment = findViewById(R.id.selector_fragment);
        fragment.setVisibility(View.GONE);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.selector_fragment);
        if (currentFragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(currentFragment);
            fragmentTransaction.commit();
        }
    }

    public void drawCard() {
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

    public void endTurn() {
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

    public void clearHand() {
        mGrid.removeAllViews();
        cards.clear();
        cardDrawables.clear();
    }

    public void addToHand(ArrayList<Card> addedCards) {
        for (Card currCard : addedCards) {
            addCard(currCard);
        }
        if (cards.size() - addedCards.size() <= MAXCARDINHAND && cards.size() > MAXCARDINHAND) {
            rearrangeHandTooBig();
        }
    }

    public void setAvailableActions(ArrayList<String> actions) {
        availableActions = actions;
        //canPlayCard = availableActions.contains("drawCards");
    }

    public void addPlayerToList(String addedPlayer) {
        //ensure no player is added twice
        if (!allPlayers.contains(addedPlayer)) {
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

    public void removePlayerFromList(String playerToBeRemoved) {
        if (allPlayers.contains(playerToBeRemoved)) {
            LinearLayout playerList = findViewById(R.id.ll_playerlist);

            for (int i = 0; i < playerList.getChildCount(); ) {
                View v = playerList.getChildAt(i);

                if (v instanceof CardView && v.getTag().equals(playerToBeRemoved)) {
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

        if (canDrawCard) {
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

    public void goBackToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message) {

    }

    @Override
    public void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message) {
        if(!message.getBody().getReason().equals("finish")) {
            goBackToMainMenu();
        }
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
        if (message.getBody().getSuccess().equals("true")) {
            setTopPlayedCard(lastPlayedCard);
            if (lastPlayedCard != null) {
                removeCardFromHand(lastPlayedCard);
            }
        }
    }

    @Override
    public void onSendState(EinzMessage<EinzSendStateMessageBody> message) {
        currentGlobalState = message.getBody().getGlobalstate();
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
        Log.e("Test", "points" + message.getBody().getPoints());
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
            if (points != null) {
                int intPoints = Integer.parseInt(points);
                tv_points.setText(playerPoints.get(player));
                usercard.setTag(intPoints);

                if (intPoints < winnerPoints) {
                    winner = player;
                    winnerPoints = intPoints;
                }


                // add view
                //winningPlayers.addView(usercard);
                if (winningPlayers.getChildCount() > 0) {
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
        }
        ((TextView) findViewById(R.id.tv_winner)).setText(winner + " won!");

    }

    @Override
    public void setHand(ArrayList<Card> hand) {
        // Log.w("PlayerActivity", "setHand is currently enabled. This means that the cards for debugging will not be shown.");
        // to disable, just comment out the following four lines
        int numberOfCardsBefore = cards.size();
        if (!checkCardsStillValid(hand)) {
            clearHand();
            addToHand(hand);
            if (numberOfCardsBefore > MAXCARDINHAND && cards.size() <= MAXCARDINHAND) {
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

            /*
            Context context = getApplicationContext();
            CharSequence text = "It's your turn " + ourClient.getUsername();
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            */
            findViewById(R.id.ll_its_your_turn).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_its_your_turn).setVisibility(View.GONE);
        }

        if (allPlayers.contains(playerThatStartedTurn)) {
            LinearLayout playerList = findViewById(R.id.ll_playerlist);

            for (int i = 0; i < playerList.getChildCount(); i++) {
                View v = playerList.getChildAt(i);

                if (v instanceof CardView && v.getTag().equals(playerThatStartedTurn)) {
                    // Do something
                    ((CardView) v).setCardBackgroundColor(getResources().getColor(R.color.blue_dark));
                } else if (v instanceof CardView) {
                    ((CardView) v).setCardBackgroundColor(getResources().getColor(R.color.blue_default));
                }
            }
        }


    }

    @Override
    public void onInitGame(EinzMessage<EinzInitGameMessageBody> message) {
        ArrayList<String> playerList = message.getBody().getTurnOrder();

        for (String currPlayer : playerList) {
            addPlayerToList(currPlayer);
            double orientation = Math.random() * 2 * Math.PI;
            orientationOfPlayer.put(currPlayer, orientation);
        }
        ruleMapping = new HashMap<>();
        Iterator<String> cardIDs = message.getBody().getCardRules().keys();
        try {
            while (cardIDs.hasNext()) {
                String cardID = cardIDs.next();
                ArrayList<BasicCardRule> rulesForCard = new ArrayList<>();
                JSONArray jsonRules = message.getBody().getCardRules().getJSONArray(cardID);
                for (int i = 0; i < jsonRules.length(); i++) {
                    String ruleName = jsonRules.getJSONObject(i).getString("id");
                    BasicCardRule ruleObject = (BasicCardRule) EinzSingleton.getInstance().getRuleLoader().getInstanceOfRule(ruleName);
                    rulesForCard.add(ruleObject);
                }
                ruleMapping.put(cardID, rulesForCard);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setNumCardsInHandOfEachPlayer(HashMap<String, Integer> numCardsInHandOfEachPlayer) {
        for (String currPlayer : allPlayers) {
            Integer numOfCurrplayerCards = numCardsInHandOfEachPlayer.get(currPlayer);
            if (numOfCurrplayerCards == null) {
                numOfCurrplayerCards = 0;
            } // added this so it doesn't crash. does =0 make sense?
            LinearLayout playerList = findViewById(R.id.ll_playerlist);
            View cardViewOfPlayer = playerList.findViewWithTag(currPlayer);

            if (cardViewOfPlayer instanceof CardView) {
                View textViewOfNrOfCards = cardViewOfPlayer.findViewById(R.id.tv_nr_of_cards);
                if (textViewOfNrOfCards instanceof TextView) {
                    ((TextView) textViewOfNrOfCards).setText(numOfCurrplayerCards.toString());
                }
            } else if (cardViewOfPlayer == null) {
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
        if (!checkCardListIdentical(stack, cardStack)) {
            Card sndCard = null;

            final Card topCard = stack.get(stack.size() - 1);

            setTopPlayedCard(topCard);

            cardStack = stack;
        }
    }

    @Override
    public void onKeepaliveTimeout() {
        goBackToMainMenu();
        // TODO: what to do when lost connection? probably return to main menu
    }

    @Override
    public void onSendPlayParameters(JSONObject playParameters) {
        JSONObject wishColorRule;

        wishColorRule = playParameters.optJSONObject("Wish Color");

        String wishedColor;

        if (wishColorRule != null) {
            wishedColor = wishColorRule.optString("wishForColor");
        } else {
            wishedColor = null;
        }

        if (wishedColor != null) {
            if (isValidColor(wishedColor)) {
                frameColor = wishedColor.toUpperCase();
            } else {
                frameColor = "NONE";
            }
        } else {
            frameColor = "NONE";
        }

    }

    private boolean isValidColor(String inColor) {
        inColor = inColor.toUpperCase();
        for (CardColor color : CardColor.values()) {
            if (color.color.equals(inColor)) {
                return true;
            }
        }
        return false;//   inColor.equals("BLUE") || inColor.equals("GREEN") || inColor.equals("YELLOW") || inColor.equals("RED");
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

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
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

            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
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
                        final int index = calculateNewIndex(mGrid, event.getX(), event.getY());
                        // remove the view from the old position
                        if (mGrid.indexOfChild(view) != index) {
                            mGrid.removeView(view);
                            // and push to the new
                            // card got removed in the meantime if not true
                            if (mGrid.getChildCount() < cards.size()) {
                                mGrid.addView(view, index);
                            }
                            //same shit for the other view
                            View view2 = mGridScrollable.findViewWithTag(view.getTag());
                            if (view2 != null) {

                                if (index < mGridScrollable.getChildCount()) {
                                    mGridScrollable.removeView(view2);

                                    // card got removed in the meantime if not true
                                    if (mGridScrollable.getChildCount() < cards.size()) {
                                        mGridScrollable.addView(view2, index);
                                    }
                                }
                            }
                        }

                        break;
                    case DragEvent.ACTION_DROP:
                        view.setVisibility(View.VISIBLE);

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        //view.setVisibility(View.VISIBLE);

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
                        final int index = calculateNewIndex(mGridScrollable, event.getX(), event.getY());
                        // remove the view from the old position
                        if (mGrid.indexOfChild(view) != index) {

                            mGridScrollable.removeView(view);
                            // and push to the new
                            mGridScrollable.addView(view, index);
                            //same for other
                            View view2 = mGrid.findViewWithTag(view.getTag());
                            if (view2 != null) {
                                if (index < mGrid.getChildCount()) {
                                    mGrid.removeView(view2);

                                    mGrid.addView(view2, index);
                                }
                            }
                        }
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

    class CustomDragShadowBuilder extends View.DragShadowBuilder {
        CustomDragShadowBuilder(View v) {
            super(v);
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            final View view = getView();
            if (view != null) {
                shadowSize.set(view.getWidth() * 2, view.getHeight() * 2);
                shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y);
            }
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            final View view = getView();
            if (view != null) {
                canvas.scale(2, 2);
                canvas.translate(0, 0);
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

                        //setlastplayedCard((Card)tmpView.getTag());
                        //remove card from inner cardlist
                        playCard((Card) tmpView.getTag());

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
}
