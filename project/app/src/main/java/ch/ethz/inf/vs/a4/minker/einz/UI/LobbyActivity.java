package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import ch.ethz.inf.vs.a4.minker.einz.*;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.RulesContainer;
import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.CardRuleChecker;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzStartGameMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.*;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;
import info.whitebyte.hotspotmanager.WifiApManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

// How to send a message:
// this.ourClient.getConnection().sendMessage()

/**
 * Lobby List. corresponds to screen 3 in our proposal.
 * Can be started either from the server device or from a client-only device.
 * Pass this Activity the following intent extra information:
 * Both:
 * "host" - boolean -     whether this device is hosting the server
 * "username" - String -  the username the user entered
 * "role" - String  -     Currently either "spectator" or "player"
 * Client-only:
 * "serverPort" - int -   on which port the server is listening
 * "serverIP" - String -  at which IP the server is located
 */
public class LobbyActivity extends FullscreenActivity implements LobbyUIInterface, View.OnClickListener, ServerActivityCallbackInterface {
    // implement some interface so that the client can update this

    private ThreadedEinzServer server; // there should be only one
    private Thread serverThread;
    private ServerFunction serverLogicInterface;

    private EinzClient ourClient;
    private String serverIP;
    private int serverPort;

    private HandlerThread backgroundThread = new HandlerThread("Networking"); // one background thread instead of many short-lived

    private boolean host; // if this device is hosting the server
    private String username;
    private String role;
    private String adminUsername; // which user was chosen as admin by the server
    private Looper backgroundLooper;
    private Handler backgroundHandler; // use this to schedule background tasks

    // Q: what if the host is not the first user to connect? stop server and restart?
    // A: No. the host is almost the first to connect unless somebody is able to pinpoint very exactly when to connect,
    //    because the server tells the host client that it needs to connect

    private boolean inSettingMode = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Log.d("TEMP", "onCreate");

        Intent intent = getIntent();
        this.host = intent.getBooleanExtra("host", false);
        this.username = intent.getStringExtra("username");
        this.role = intent.getStringExtra("role");

        if (this.host) {
            startServer();
            //((CardView) findViewById(R.id.cv_lobby_server_info)).setCardBackgroundColor(Color.YELLOW); // CYAN for client, Yellow for server. yey.
            findViewById(R.id.btn_start_game).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_settings_button).setVisibility(View.VISIBLE);
            findViewById(R.id.iv_settings_button).setOnClickListener(this);
            findViewById(R.id.btn_save_settings).setOnClickListener(this);
            findViewById(R.id.btn_lobby_default_rules_toggle).setOnClickListener(this);
            // wait for server to tell us it's ready so we can connect in onLocalServerReady()

            // initialize layouts for settings
            globalRuleList = findViewById(R.id.ll_global_rules);
            cardList = findViewById(R.id.ll_card_rules);

        } else {
            // still display the IP/PORT info so that they can tell their friends

            /// Option to hide the infobox
            ///((CardView) findViewById(R.id.cv_lobby_server_info)).setVisibility(View.GONE);
            findViewById(R.id.btn_start_game).setVisibility(View.GONE);
            findViewById(R.id.iv_settings_button).setVisibility(View.GONE);

            // get info
            this.serverPort = intent.getIntExtra("serverPort", -1);
            this.serverIP = intent.getStringExtra("serverIP");
            // set UI to display this
            String ip = "IP: " + this.serverIP;
            String p = "PORT: " + String.valueOf(this.serverPort);
            ((TextView) findViewById(R.id.tv_lobby_ip)).setText(ip);
            ((TextView) findViewById(R.id.tv_lobby_port)).setText(p);
            //((CardView) findViewById(R.id.cv_lobby_server_info)).setCardBackgroundColor(Color.CYAN); // CYAN for client, Yellow for server. yey.

            // this client will only be shown in the list once the server told it that it was registered.

            // show that it is connecting
            addLobbyListUser(this.username, this.role + "   (Connecting...)");
            // this will be purged once the client receives the first UpdateLobbyList

            // start client. Because we specify "host" as false, the client will automatically register
            this.ourClient = new EinzClient(this.serverIP, this.serverPort, getApplicationContext(), this.username, this.role, false, this);
            new Thread(this.ourClient).start();
        }

        ///debug_populate_lobbylist();

        // initialize background threading for small sending tasks
        this.backgroundThread.start();
        this.backgroundLooper = this.backgroundThread.getLooper();
        this.backgroundHandler = new Handler(this.backgroundLooper);

        final Button startGameButton = (Button) findViewById(R.id.btn_start_game);
        startGameButton.setEnabled(true);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                onStartGameButtonClick();
                Handler mainHandler = new Handler(getMainLooper());
                mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startGameButton.setEnabled(true);
                    }
                }, 600); // set enabled again after some time because the problem would have been minuscule in the first place
                // and it is inconvenient to have to go back after getting "Plz be at least one player"
            }
        });

        //if(ourClient!=null && ourClient.getActionCallbackInterface()!=null){ourClient.getActionCallbackInterface().setLobbyUI(this);}

    }

    /**
     * Called by the onClickListener of the startGame button
     */
    private void onStartGameButtonClick() {
        if (!this.host)
            return;

        // send startGame message before that activity starts
        EinzMessageHeader header = new EinzMessageHeader("startgame", "StartGame");
        EinzStartGameMessageBody body = new EinzStartGameMessageBody();
        final EinzMessage<EinzStartGameMessageBody> startGameMessage = new EinzMessage<>(header, body);
        Runnable startGame = new Runnable() {
            @Override
            public void run() {
                ourClient.getConnection().sendMessageRetryXTimes(2, startGameMessage);
            }
        };
        this.backgroundHandler.post(startGame);

        // <UglyHack> // no more here because clemens calls this on init game
        // startGameUIWithThisAsContext();

    }

    /**
     * Starts the gameUI {@link PlayerActivity} with this Activity as parent context
     */
    public void startGameUIWithThisAsContext() {
        // <UglyHack>
        // read EinzConstants.ourClientGlobal's javadocs to understand this. Basically, I cannot implement parcelable for PrintWriter, and
        // thus not for EinzClient
        Intent intent;
        if (this.role.equals("player")) {
            intent = new Intent(this, PlayerActivity.class);
            EinzSingleton.getInstance().setEinzClient(this.ourClient);
            startActivity(intent);
        } else if (this.role.equals("spectator")) {
            intent = new Intent(this, SpectatorActivity.class);
            EinzSingleton.getInstance().setEinzClient(this.ourClient);
            startActivity(intent);
        }

    }

    @Override
    public void onKeepaliveTimeout() {
        this.onBackPressed();
    }

    private void debug_populate_lobbylist() {

        // <Debug>
        ArrayList<String> pl = new ArrayList<>();
        pl.add("some player");
        ArrayList<String> sp = new ArrayList<>();
        sp.add("some spectator");
        pl.add("admin player");
        setAdmin("admin player");
        setLobbyList(pl, sp);
        //</Debug>
    }

    /**
     * adds the user to the list and highlights him if he's (previously set using {@link #setAdmin(String)}) admin.
     *
     * @param username
     * @param role
     */
    private void addLobbyListUser(final String username, String role) {
        LinearLayout lobbyList = findViewById(R.id.ll_lobbylist);

        CardView usercard = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_lobbylist_element, lobbyList, false);
        // false because don't add view yet - I first want to set some text

        TextView tv_username = usercard.findViewById(R.id.tv_lobbylist_username);
        TextView tv_role = usercard.findViewById(R.id.tv_lobbylist_role);

        // set text
        tv_username.setText(username);
        tv_role.setText(role);
        ImageView iconRole = usercard.findViewById(R.id.icn_role);

        if (role.contains("spectator")) {
            iconRole.setImageResource(R.drawable.ic_spectator_green_darker_24dp);
        } else if (role.contains("player")) {
            iconRole.setImageResource(R.drawable.ic_person_green_darker_24dp);
        } else {

        }

        // highlight admin
        if (username.equals(this.adminUsername)) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                usercard.setCardBackgroundColor(getResources().getColor(R.color.red_default, getApplicationContext().getTheme()));
                ((ImageView) usercard.findViewById(R.id.icn_role)).getDrawable().setColorFilter(getResources().getColor(R.color.red_darker, getApplicationContext().getTheme()), PorterDuff.Mode.SRC_ATOP);
                ((ImageView) usercard.findViewById(R.id.btn_lobby_kick)).getDrawable().setColorFilter(getResources().getColor(R.color.red_darker, getApplicationContext().getTheme()), PorterDuff.Mode.SRC_ATOP);
                ((TextView) usercard.findViewById(R.id.tv_lobbylist_username)).setTextColor(getResources().getColor(R.color.red_darker, getApplicationContext().getTheme()));
                ((TextView) usercard.findViewById(R.id.tv_lobbylist_role)).setTextColor(getResources().getColor(R.color.red_darker, getApplicationContext().getTheme()));
            } else {
                usercard.setCardBackgroundColor(getResources().getColor(R.color.red_default));
                ((ImageView) usercard.findViewById(R.id.icn_role)).getDrawable().setColorFilter(getResources().getColor(R.color.red_darker), PorterDuff.Mode.SRC_ATOP);
                ((ImageView) usercard.findViewById(R.id.btn_lobby_kick)).getDrawable().setColorFilter(getResources().getColor(R.color.red_darker), PorterDuff.Mode.SRC_ATOP);
                ((TextView) usercard.findViewById(R.id.tv_lobbylist_username)).setTextColor(getResources().getColor(R.color.red_darker));
                ((TextView) usercard.findViewById(R.id.tv_lobbylist_role)).setTextColor(getResources().getColor(R.color.red_darker));
            }


        }

        if (this.host) {
            // show kick button // TODO: hide kick button for kicking the admin user itself?
            View kickButtonFrame = usercard.findViewById(R.id.fl_lobby_kick_frame);
            kickButtonFrame.setVisibility(View.VISIBLE);
            // setup onclick listener
            Context context = this;
            View kickButton = usercard.findViewById(R.id.btn_lobby_kick);
            kickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    kick(username);
                }
            });
        }

        // add view
        lobbyList.addView(usercard);


    }

    /**
     * This method is only for admins.
     * Sends kick request in new thread
     *
     * @param username who to kick
     */
    private void kick(final String username) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ourClient.sendKickRequest(username);
            }
        };
        /* // old version
        (new Thread(r)).start();
        */
        this.backgroundHandler.post(r);
    }

    /**
     * remove all usercards from the lobby list (and all other content of the list as well)
     */
    private void clearLobbyList() {
        LinearLayout lobbyList = findViewById(R.id.ll_lobbylist);
        lobbyList.removeAllViews();
    }


    /**
     * clears the lobby list, rewrites it based on the parameters
     *
     * @param players
     * @param spectators
     */
    @Override
    public void setLobbyList(ArrayList<String> players, ArrayList<String> spectators) {
        clearLobbyList();

        // first add all players
        for (String player : players) {
            addLobbyListUser(player, "player");
        }

        // then add all spectators
        for (String spectator : spectators) {
            addLobbyListUser(spectator, "spectator");
        }

    }

    @Override
    public void setAdmin(String username) {
        Log.d("LobbyActivity", "set admin to " + username);
        this.adminUsername = username;
    }

    @Override
    public void onRegistrationFailed(EinzRegisterFailureMessageBody body) {
        /*
        - "not unique" if the same username was already registered
        - "already registered" if the same connection already has registered a username
        - "invalid" if the username is the empty string or "server". Or if the username contains invalid characters. One invalid character is the Tilde, which is reserved to identify non-username-strings
        - "lobby full" if the server decided to fixate the number of players or spectators and the game has not yet started (otherwise, the server wouldn't react at all).
        - "game already in progress"
         */
        String toastMsg;
        switch (body.getReason()) {
            case "not unique":
            case "invalid":
            case "already registered":
                // make user change name
                // only the thread that created the view is allowed to update them
                toastMsg = "Please choose a different name. Yours is " + body.getReason();
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                cleanupActivity();
                this.onBackPressed();
                return;
            case "lobby full":
            case "game already in progress":
                // tell user that he cannot join
                toastMsg = "You cannot join, sorry. " + body.getReason();
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                this.onBackPressed();
                return;

            default:
                toastMsg = "Something went wrong.";
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                this.onBackPressed();
                return;

        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_settings_button: {
                onSettingsClick();
                break;
            }

            case R.id.btn_save_settings: {
                onSettingsSaveClick();
                break;
            }

            case R.id.btn_lobby_default_rules_toggle: {
                onDefaultRulesToggle((ToggleButton) view);
                break;
            }
        }
    }

    private void onDefaultRulesToggle(ToggleButton button) {
        toggleDefaultRules(button.isChecked(), false);
    }

    private void toggleDefaultRules(boolean defaultRulesOn, boolean needToToggleUIButtonText){
        // show in UI
        if(needToToggleUIButtonText) {
            ToggleButton toggleButton = ((ToggleButton) findViewById(R.id.btn_lobby_default_rules_toggle));
            if (toggleButton != null) {
                toggleButton.setChecked(defaultRulesOn);
            }
        }
        Log.d("LobbySettings", "using default rules: "+defaultRulesOn);

        if(defaultRulesOn){
            this.usingDefaultRules = true;
            setUIToDefaultSettings();
            this.rulesContainer = RulesContainer.getDefaultRulesInstance();
            try {
                String msg = this.rulesContainer.toMessage().getBody().toJSON().toString();
                Log.d("LobbySettings", "using default rules: " + msg);

            } catch (JSONException e) {
                Log.w("LobbySettings", "Failed to log");
                e.printStackTrace();
            }
        } else {
            this.usingDefaultRules = false;
        }
    }

    private void setUIToDefaultSettings() {
        setUIToGivenSettings(RulesContainer.getDefaultRulesInstance(), true);
    }

    private void setUIToGivenSettings(RulesContainer givenSettings, boolean defaultSettingsWerePassed) {
        usingDefaultRules = defaultSettingsWerePassed;
        // cardsM should be unchanged and contain all cards
        // globalRulesM should be unchanged and contains all globalRules
        // cardRulesM is only changed in the end of the popup so we do not need to re-set this as long as we re-set the UI
        RulesContainer container = givenSettings;

        // set card numbers to default
        for (View view : cardsM.keySet()) {
            EditText et = view.findViewById(R.id.et_number_of_cards);
            if (et != null) {
                listenOnAnythingMaybeChanged=false;
                et.setText(String.valueOf(container.getNumberOfCards(cardsM.get(view).getID())));
                listenOnAnythingMaybeChanged=true;
            }
        }

        // check global rules as default
        for (View view : globalRulesM.keySet()) {
            BasicGlobalRule rule = globalRulesM.get(view);
            BasicGlobalRule defaultRule = container.getGlobalRule(rule.getName());
            if (defaultRule == null) {
                CheckBox checkBox = ((CheckBox) view.findViewById(R.id.cb_global_rule));
                listenOnAnythingMaybeChanged=false;
                checkBox.setChecked(false);
                listenOnAnythingMaybeChanged=true;
            } else {
                listenOnAnythingMaybeChanged=false;
                ((CheckBox) view.findViewById(R.id.cb_global_rule)).setChecked(true);
                listenOnAnythingMaybeChanged=true;
                // show default parameters
                try {
                    EditText et = view.findViewById(R.id.et_global_rule_param);
                    if (et != null && rule instanceof ParametrizedRule && defaultRule instanceof ParametrizedRule) {
                        Iterator<String> keys = ((ParametrizedRule) rule).getParameter().keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            String r = ((ParametrizedRule) rule).getParameter().optString(key);
                            String def = ((ParametrizedRule) defaultRule).getParameter().optString(key);
                            /// this only works when the rule was correctly initialized and the editText as well
                            // fix idea: check also if edittext string is empty
                            String ettext = et.getText().toString();
                            if (r.equals(ettext) || ettext.equals("")) {
                                listenOnAnythingMaybeChanged=false;
                                et.setText(def);
                                listenOnAnythingMaybeChanged=true;
                            }
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "resetting UI to stored rules failed", Toast.LENGTH_SHORT).show();
                    Log.w("LobbySettings", "Failed to load parameters for rule while fitting UI to given rules");
                }
            }
        }


        /* //that(now deleted code) doesn't work... changing the popup loader now so that it always sets the current rules of the rulesContainer
        // cardrules will always display what currently is set in the cardRulesM
        // the below code is copypasted and modified from the initialization
        */

    }


    public void onSettingsClick() {
        findViewById(R.id.ll_lobbyframe).setVisibility(View.GONE);
        findViewById(R.id.ll_settingsframe).setVisibility(View.VISIBLE);
        initialiseMappingFromViewToRules();
        inSettingMode = true;
    }

    public void onSettingsSaveClick() {
        saveAndSend();
        findViewById(R.id.ll_settingsframe).setVisibility(View.GONE);
        findViewById(R.id.ll_card_popup_settingsframe).setVisibility(View.GONE);
        findViewById(R.id.ll_lobbyframe).setVisibility(View.VISIBLE);
        inSettingMode = false;
    }

    private void saveAndSend() {
        saveSettings();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ourClient.getConnection().sendMessageRetryXTimes(3, rulesContainer.toMessage());
            }
        }).start();
    }

    /**
     * assumes the fields globalRulesM and cardsM and cardRulesM are appropriately set and stores the settings in the rulesContainer
     */
    private void saveSettings() {
        if (usingDefaultRules) {
            this.rulesContainer = RulesContainer.getDefaultRulesInstance();
            EinzSingleton.getInstance().setLastRulesSavedContainer(this.rulesContainer); // make them load the default version
            return;
        }

        // TODO: write settings as profile (that can be loaded again after app restart) to disk?
        Log.w("LobbySettings", "SaveSettings is probably unfinished if this log is still here.");

        // store all unchanged card settings in cardRulesM so they don't get lost
        for(Card card : cardsM.values()) {
            String cardID = card.getID();
            ArrayList<BasicCardRule> list = cardRulesM.get(card);
            if(list==null || list.equals(new ArrayList<BasicCardRule>())){
                // if nothing was stored yet for this card, store everything from the settings that would have been shown if the UI popup were to have been opened
                // #grammar

                ArrayList<BasicCardRule> cardRuleList = this.rulesContainer.getListOfCardRulesForCard(cardID, ruleLoader);
                cardRulesM.put(card, cardRuleList);
            }
        }

        this.rulesContainer = new RulesContainer(); // clear old settings

        for (View view : globalRulesM.keySet()) {
            CheckBox cb = view.findViewById(R.id.cb_global_rule);
            if (cb != null && cb.isChecked()) {
                BasicGlobalRule rule = globalRulesM.get(view);
                if (rule instanceof ParametrizedRule && rule!=null) {
                    EditText et = view.findViewById(R.id.et_global_rule_param);
                    if(et!=null) {
                        String firstParamName = ((ParametrizedRule) rule).getParameterTypes().keySet().iterator().next();
                        JSONObject parameters = new JSONObject();
                        try {
                            parameters.put(firstParamName, et.getText().toString());
                            ((ParametrizedRule) rule).setParameter(parameters);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.w("LobbySettings", "failed to read parameter of a parametrized global rule :"+rule.getName());
                    }
                }
                this.rulesContainer.addGlobalRule(globalRulesM.get(view));
            }
        }

        for (View view : cardsM.keySet()) {
            Integer num = 0;
            try {
                EditText et = (EditText) view.findViewById(R.id.et_number_of_cards);
                num = (Integer.valueOf((et).getText().toString()));
            } catch (Exception e) {
                Log.w("LobbySettings", "number of cards was not a number");
                num = 0;
            }
            if (num != 0) {
                this.rulesContainer.addCard(cardsM.get(view).getID(), num);
            } // only send cards that make sense to send
            else {
                this.rulesContainer.removeCard(cardsM.get(view).getID());
            } // if 0 make sure the card is not set
        }

        for (Card card : cardRulesM.keySet()) {
            ArrayList<BasicCardRule> rulesToAdd = new ArrayList<>();
            for (BasicCardRule cardRule : cardRulesM.get(card)) {
                // for all cardrules belonging to card, add them
                rulesToAdd.add(cardRule);
            }
            this.rulesContainer.setCardRulesKeepNumber(rulesToAdd, card.getID());// delete all previous cardrules for this card and set the ones we store anew
        }

        // store settings in EinzSingleton to load them again when the popup is reopened
        EinzSingleton.getInstance().setLastRulesSavedContainer(new RulesContainer(this.rulesContainer)); // store a deep copy that lives now independently in the singleton
    }

    /**
     * An overly sensitive reaction: whenever a player changed something, he is not using default rules. If he maybe changed something he isn't either.
     * Hopefully called on all onClick events of the contents of these settings
     */
    private void onAnythingMaybeChanged(){
        // only do that if the user changed something, not when loading default rules
        if(!listenOnAnythingMaybeChanged){return;}
        toggleDefaultRules(false, true);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!inSettingMode) {
            cleanupActivity();
            super.onBackPressed();
        } else {
            onSettingsSaveClick();
            final LinearLayout myPopupLL = (LinearLayout) LayoutInflater.from(LobbyActivity.this).inflate(R.layout.linearlayout_settings_cardrule_popup, (LinearLayout) findViewById(R.id.ll_card_popup_settingsframe), false);
            final LinearLayout settingsFrame = ((LinearLayout) findViewById(R.id.ll_card_popup_settingsframe));
            settingsFrame.removeView(myPopupLL);
            findViewById(R.id.btn_save_settings).setVisibility(View.VISIBLE);
        }
        //TODO @Eric/@Chris Check if in SettingsState or LobbyState and do SaveAndGoBack if in SettingsState
    }

    @Override
    public void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message) {
        if (!host) {
            goBackToMainMenu();
        }
    }

    public void goBackToMainMenu() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ourClient != null && ourClient.getActionCallbackInterface() != null)
            this.ourClient.getActionCallbackInterface().setLobbyUI(this);

        Button startGameButton = (Button) findViewById(R.id.btn_start_game);
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setEnabled(false);
                onStartGameButtonClick();
            }
        });
        startGameButton.setEnabled(true);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.ourClient != null && this.ourClient.getActionCallbackInterface() != null) {
            this.ourClient.getActionCallbackInterface().setLobbyUI(null); // make sure no callbacks to this activity are executed
        }
    }

    /**
     * stops server if there is one on this device. <br>
     * stops client.
     */
    private void cleanupActivity() {
        // stop server on back button
        if (this.host && this.server != null && !this.server.isDead()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (server != null) {
                        server.shutdown();
                        server = null;
                    }
                }
            };
            /*//old version
            (new Thread(r)).start();
            */
            // don't run this on the main thread. networking is not allowed on the main thread
            this.backgroundHandler.post(r);
        }

        if (this.ourClient != null && !this.ourClient.isDead()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (ourClient != null) {
                        ourClient.shutdown(true);
                        ourClient = null;
                    }
                }
            };
            //old version
            // /*(new Thread(r)).start();*/
            this.backgroundHandler.post(r);
        }
    }

    /**
     * set server serverIP and serverPort to be displayed in ui
     *
     * @param einzServer where the IP and Port info come from
     */
    private void setIPAndPort(ThreadedEinzServer einzServer) {
        this.serverIP = getIP();
        String ip = "IP: " + this.serverIP;
        ((TextView) findViewById(R.id.tv_lobby_ip)).setText(ip);
        this.serverPort = einzServer.getPORT();
        String p = "PORT: " + String.valueOf(serverPort);
        ((TextView) findViewById(R.id.tv_lobby_port)).setText(p);

        // <Debug>
        //this.serverIP="127.0.0.1";
        //this.serverPort=8080;
        // </Debug>
    }

    private void startServer() {
        Log.d("serverSetupActivity", "startServer was pressed");
        if (serverThread == null) { // only create one server
            this.serverLogicInterface = new ServerFunction(); // Fabians Part
            // try server on port 8080, but use any other free port if it is not available
            server = new ThreadedEinzServer(this.getApplicationContext(), 8080, this, this.serverLogicInterface);
            setIPAndPort(server);
            server.setDEBUG_ONE_MSG(false); // set to true to let server generate messages on same host
            serverThread = new Thread(server);
            serverThread.start();
        }

    }

    /**
     * @return the probably used IP address
     */
    public String getIP() {
        // display serverPort
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation") // https://stackoverflow.com/a/20846328/2550406
                String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        if (ip.equals("0.0.0.0") || ip.equals("") || ip.equals("null")) {
            // not connected via WIFI, use something else
            try {
                ip = getLocalIpAddress(); // use the code of some stackoverflow dude.
            } catch (SocketException e) {
                ip = e.getMessage();
                e.printStackTrace();
            }
        } else {
            Log.d("LobbyActivity/IP/1stTry", "wlan address: " + ip);
        }
        return ip;
    }

    // https://stackoverflow.com/a/30183130/2550406
    private String getLocalIpAddress() throws SocketException {
        WifiManager wifiMgr = (WifiManager) this.getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        if (wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            String wifiIpAddress = String.format(Locale.US, "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
            Log.d("LobbyActivity/IP", "wlan address: " + wifiIpAddress);
            if (!wifiIpAddress.equals("0.0.0.0"))
                return wifiIpAddress;
        }

        // if no wlan address because not connected or in hotspot mode.
        // the default ip of hotspot server would be 192.168.43.1 if the device manufacturer did not change it
        // Check if hotspot mode using https://github.com/nickrussler/Android-Wifi-Hotspot-Manager-Class

        WifiApManager wifiApManager = new WifiApManager(this);
        boolean hotspotModeOn = wifiApManager.isWifiApEnabled();
        if (hotspotModeOn) {
            Toast.makeText(this, "You seem to be using a hotspot. Your IP within your own AP network is by default 192.168.43.1", Toast.LENGTH_LONG).show();
        }

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                Log.i("LobbyActivity/IP", "inetAddress.getHostAddress(): " + inetAddress.getHostAddress());

                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    Log.i("LobbyActivity/IP", "return inetAddress.getHostAddress(): " + inetAddress.getHostAddress());
                    return inetAddress.getHostAddress();
                }

            }
        }

        return null;
    }

    @Override
    public void updateNumClientsUI(int num) {
        // don't care
    }

    @Override
    public void onLocalServerReady() {
        Log.d("LobbyActivity", "local server ready. Connecting...");
        //    setIPAndPort(server);
/*        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        connectClientToLocalServer();
    }


    /**
     * When you are the host and the first client-handler in the server is ready to receive the register message
     */
    @Override
    public void onFirstESCHReady() {
        this.ourClient.onServersideHandlerReady();
    }

    private void connectClientToLocalServer() {
        this.ourClient = new EinzClient(this.serverIP, this.serverPort, this.getApplicationContext(), this.username, this.role, this.host, this);
        this.ourClient.run();
        ///new Thread(this.ourClient).start();
        // from now on, the client has the program flow and needs to update the UI appropriately
    }

    // -------------------------- SETTINGS ---------------------------------

    // SETTINGS Variables
    private RulesContainer rulesContainer = new RulesContainer();
    private HashMap<View, BasicGlobalRule> globalRulesM = new HashMap<>(); // contains the rules that can be reused by the UI. Never changed after initialization
    private HashMap<View, Card> cardsM = new HashMap<>(); // contains the rules that can be reused by the UI. Never changed after initialization
    private HashMap<Card, ArrayList<BasicCardRule>> cardRulesM = new HashMap<>(); // contains the cardRules which correspond to specific cards. Changed after every popup close
    private RuleLoader ruleLoader = EinzSingleton.getInstance().getRuleLoader();
    private CardLoader cardLoader = EinzSingleton.getInstance().getCardLoader();
    private LinearLayout globalRuleList; // the list for global rules
    private LinearLayout cardList; // the list for card views
    private boolean usingDefaultRules = false; // if true, saveSettings takes a shortcut when storing
    private boolean listenOnAnythingMaybeChanged = true; // look at the current usage of this before naively using it!

    // globalRules each have one View which can be toggled, so they are mapped <View, Rule>
    // Cards each have one View which has multiple settings, so they are mapped <View, Card>
    // CardRules each have one inner View per (Cardview, Cardruleview) combination, so they are mapped <Card, <View, Rule>>
    // The reason we use rule instances is that you can set parameters on them.

    private void initialiseMappingFromViewToRules() {
        // remove all views from the settings part
        globalRuleList.removeAllViews();
        cardList.removeAllViews();
        cardsM.clear();
        globalRulesM.clear();
        RulesContainer oldSettings = EinzSingleton.getInstance().getLastRulesSavedContainer();
        if(oldSettings!=null && oldSettings.getHeader()!=null){
            this.rulesContainer = new RulesContainer(oldSettings); // deep copy to avoid changing the old settings before saving
            usingDefaultRules = false;
        } else {
            oldSettings = null;
            usingDefaultRules = true;
            this.rulesContainer = RulesContainer.getDefaultRulesInstance(); // should already be a deep copy
        }

        // for all cards, store them in cardsM
        for (String cardID : cardLoader.getCardIDs()) {
            Card card = cardLoader.getCardInstance(cardID);
            View cardView = generateCardView(card);
            this.cardsM.put(cardView, card);
            this.cardRulesM.put(card, new ArrayList<BasicCardRule>());
        }

        android.support.v4.util.ArraySet<BasicCardRule> allCardRules = new android.support.v4.util.ArraySet<>();

        // for all GlobalRules, store them in globalRulesM
        // for all BasicCardRules, store them in every Card-representing view
        for (String ruleName : ruleLoader.getRulesNames()) {
            BasicRule rule = ruleLoader.getInstanceOfRule(ruleName);
            if (rule instanceof BasicGlobalRule) {
                View view = generateGlobalRuleView(rule);
                this.globalRulesM.put(view, (BasicGlobalRule) rule);
            } else if (rule instanceof BasicCardRule) {
                // add cardruleview to all card views
                for (View cView : this.cardsM.keySet()) {
                    Card theCard = this.cardsM.get(cView);
                    // the cardRule views are generated onClick
                    allCardRules.add((BasicCardRule) rule); // the onClick requires this
                }
            }
            // else wth are you, rule?
        }

        // sort global rules alphabetically
        ArrayList<View> sortedGlobalRulesKeys = new ArrayList<>(globalRulesM.keySet());
        Collections.sort(sortedGlobalRulesKeys, new Comparator<View>() {
            @Override
            public int compare(View view, View t1) {
                String v0 = globalRulesM.get(view).getName();
                String v1 = globalRulesM.get(t1).getName();
                return v0.compareTo(v1);
            }
        });

        // display all global rules
        for (View view : sortedGlobalRulesKeys) {
            addViewToGlobalRulesViewsList(view);
        }

        // sort card views by card name
        ArrayList<View> cardViews = new ArrayList<>(this.cardsM.keySet());
        Collections.sort(cardViews, new Comparator<View>() {
            @Override
            public int compare(View view, View t1) {
                String id0 = ((Card) cardsM.get(view)).getName();
                String id1 = ((Card) cardsM.get(t1)).getName();
                return id0.compareTo(id1);
            }
        });

        // sort cardrules for in the popup
        ArrayList<BasicCardRule> sortedAllCardRules = new ArrayList<>(allCardRules);
        Collections.sort(sortedAllCardRules, new Comparator<BasicCardRule>() {
            @Override
            public int compare(BasicCardRule basicCardRule, BasicCardRule t1) {
                String n0 = basicCardRule.getName();
                String n1 = t1.getName();
                return n0.compareTo(n1);
            }
        });
        final ArrayList<BasicCardRule> allCardRulesf = sortedAllCardRules;

        // display all card-respective rules
        for (final View cardView : cardViews) {
            addViewToCardViewsList(cardView);

            // prepare for RULES popup
            Button btnCardRules = cardView.findViewById(R.id.btn_card_rules);
            final Card card = cardsM.get(cardView);
            btnCardRules.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final LinearLayout myPopupLL = (LinearLayout) LayoutInflater.from(LobbyActivity.this).inflate(R.layout.linearlayout_settings_cardrule_popup, (LinearLayout) findViewById(R.id.ll_card_popup_settingsframe), false);
                    final LinearLayout settingsFrame = ((LinearLayout) findViewById(R.id.ll_card_popup_settingsframe));
                    final LinearLayout popupContainer = myPopupLL.findViewById(R.id.ll_popup_container);

                    for (BasicCardRule cardRule : allCardRulesf) {
                        generateCardRuleViewAndAddToItsPopup(cardRule, cardView, popupContainer);
                    }

                    // show popup
                    settingsFrame.addView(myPopupLL);
                    settingsFrame.setVisibility(View.VISIBLE);
                    findViewById(R.id.btn_save_settings).setVisibility(View.INVISIBLE);

                    settingsFrame.findViewById(R.id.btn_save_cardrule_popup).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // when the popup is closed
                            storeCardSpecificRules(popupContainer, card); // set for all cardrules their setting in cardRulesM only if checked. if checked, store with new params
                            settingsFrame.removeView(myPopupLL);
                            findViewById(R.id.btn_save_settings).setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        }

        // initialize looks with default rules
        //      setUIToDefaultSettings();// included in toggleDefaultRules
        // and make sure the button starts with the correct text
        if(usingDefaultRules) {
            toggleDefaultRules(true, true);
        } else {
            setUIToGivenSettings(this.rulesContainer, false);
            Log.d("LobbySettings","Loaded stored settings");
        }
    }

    /**
     * Overwrites the settings for this card
     *
     * @param cardRuleContainer the parent of the card-rule-views
     * @param card
     */
    private void storeCardSpecificRules(LinearLayout cardRuleContainer, Card card) {
        ArrayList<BasicCardRule> list = new ArrayList<>();
        for (int i = 0; i < cardRuleContainer.getChildCount(); i++) {
            View view = cardRuleContainer.getChildAt(i);
            CheckBox cb = view.findViewById(R.id.cb_card_rule);
            EditText et = view.findViewById(R.id.et_card_rule_param);
            if (cb != null && et != null) {
                // That may be only some view with the same fields but different purpose, but if it has them, I can use them. That's fine by me.
                if (cb.isChecked()) {
                    BasicCardRule rule = (BasicCardRule) view.getTag();
                    if (rule instanceof ParametrizedRule) {
                        try {
                            String firstParamName = ((ParametrizedRule) rule).getParameterTypes().keySet().iterator().next();
                            JSONObject params = new JSONObject();
                            params.put(firstParamName, et.getText().toString());
                            ((ParametrizedRule) rule).setParameter(params); // TODO: handle more than one parameter

                        } catch (JSONException e) {
                            Log.e("LobbySettings", "failed to save " + rule.getName() + " for card " + card.getID());
                        }
                    }
                    list.add(rule);
                }
            }
        }
        this.cardRulesM.put(card, list);

        // store also in the current container. It will be overwritten in the end, but is used in the meantime to load rule settings for cardrules
        this.rulesContainer.setCardRulesKeepNumber(list, card.getID());
    }

    /**
     * Feel free to discard the returned view. This methods creates a view for the cardRules popup and loads it in.
     * You can call this method on every popup load
     * @param rule
     * @param cView
     * @param popupList
     * @return
     */
    private View generateCardRuleViewAndAddToItsPopup(BasicCardRule rule, View cView, LinearLayout popupList) {
        Card theCard = cardsM.get(cView);

        CardView cardRuleView = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_settings_cardrule_popup_element, popupList, false);
        cardRuleView.setTag(rule); // used in onCardRuleToggled

        CheckBox checkBox = (CheckBox) cardRuleView.findViewById(R.id.cb_card_rule);
        listenOnAnythingMaybeChanged=false;
        checkBox.setText(rule.getName());
        listenOnAnythingMaybeChanged=true;

        EditText etParams = (EditText) cardRuleView.findViewById(R.id.et_card_rule_param);
        etParams.setSingleLine();
        etParams.setVisibility(View.INVISIBLE);
        if (rule instanceof ParametrizedRule) {
            HashMap<String, ParameterType> paramTypes = new HashMap<String, ParameterType>(((ParametrizedRule) rule).getParameterTypes());
            if (paramTypes.size() >= 1) {
                String paramName = paramTypes.keySet().iterator().next(); // just get that one element out of the set
                ParameterType paramType = paramTypes.get(paramName);
                if (paramType.equals(ParameterType.NUMBER)) {
                    etParams.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    etParams.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                etParams.setVisibility(View.VISIBLE);

                BasicCardRule defaultRule = RulesContainer.getDefaultRulesInstance().getCardRule(rule.getName(), theCard.getID(), ruleLoader);
                if (defaultRule instanceof ParametrizedRule) {
                    Iterator<String> keys = ((ParametrizedRule) rule).getParameter().keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        String r = ((ParametrizedRule) rule).getParameter().optString(key);
                        String def = ((ParametrizedRule) defaultRule).getParameter().optString(key);
                        Integer def2 = ((ParametrizedRule) defaultRule).getParameter().optInt(key);
                        //Log.e("TEMP", "DOES BOTH WORK? \t  "+def+" , "+def2);
                        /// this only works when the rule was correctly initialized and the editText as well
                        // fix idea: check also if edittext string is empty
                        String ettext = etParams.getText().toString();
                        if (r.equals(ettext) || ettext.equals("")) {
                            listenOnAnythingMaybeChanged=false;
                            etParams.setText(def);
                            listenOnAnythingMaybeChanged=true;
                        }
                    }
                }
            }
            etParams.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    onAnythingMaybeChanged();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            }); // onAnythingMaybeChanged when this is edited
        }

        // load UI looks from current settings
        this.listenOnAnythingMaybeChanged=false;
        checkBox.setChecked(this.rulesContainer.containsCardRule(rule.getName(), theCard.getID()));
        //checkBox.setChecked(cardRulesM.get(theCard).contains(rule));//That approach didn't work
        this.listenOnAnythingMaybeChanged=true;

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onAnythingMaybeChanged();
            }
        });// onAnythingMaybeChanged when this is changed

        // TODO: place parameters from default rules also in cardrules ui

        if (popupList != null) {
            popupList.addView(cardRuleView);
        }
        return cardRuleView;
    }

    private void addViewToCardViewsList(View cardView) {
        cardList.addView(cardView);
    }

    private View generateCardView(final Card card) {
        CardView cardView = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_settings_cardrule_element, cardList, false);
        TextView tvCardID = (TextView) cardView.findViewById(R.id.tv_card_name);
        EditText etNumberOfCards = (EditText) cardView.findViewById(R.id.et_number_of_cards);
        Button btnCardRules = (Button) cardView.findViewById(R.id.btn_card_rules);

        etNumberOfCards.setSingleLine();
        etNumberOfCards.setInputType(InputType.TYPE_CLASS_NUMBER);
        etNumberOfCards.addTextChangedListener(new TextWatcher() { // call onAnythingMaybeChanged if this is edited
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                onAnythingMaybeChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        }); // call onAnythingMaybeChanged when the text changes

        listenOnAnythingMaybeChanged=false;
        tvCardID.setText(card.getName());
        listenOnAnythingMaybeChanged=true;

        cardView.setTag(card); // yet to be used

        return cardView;
    }

    private void addViewToGlobalRulesViewsList(View view) {
        globalRuleList.addView(view);
    }

    private View generateGlobalRuleView(BasicRule rule) {
        CardView globalRuleView = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_settings_globalrule_element, globalRuleList, false);
        // false because I don't want to add this view yet

        CheckBox checkBox = globalRuleView.findViewById(R.id.cb_global_rule);
        listenOnAnythingMaybeChanged=false;
        checkBox.setText(rule.getName()); // set description for user
        listenOnAnythingMaybeChanged=true;
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onAnythingMaybeChanged();
            }
        });

        // setting a tag because Chris said this might be useful. Don't know yet for what
        globalRuleView.setTag(rule);
        // I could use getTag to get that rule back, or find by tag to get the view

        // set parameter view
        if (!(rule instanceof ParametrizedRule)) {
            // No need for params
            EditText etParam = (EditText) globalRuleView.findViewById(R.id.et_global_rule_param);
            etParam.setVisibility(View.INVISIBLE);
        } else {
            HashMap<String, ParameterType> paramTypes = new HashMap<String, ParameterType>(((ParametrizedRule) rule).getParameterTypes());

            if (paramTypes.size() > 1) {
                Log.w("LobbySettings", "Layout does not yet support more than one parameter per rule"); // TODO: multiple parameters for one rule?
            } else {
                // the current usecase for DrawCard rule and StartWithCardsRule
                EditText etParam = (EditText) globalRuleView.findViewById(R.id.et_global_rule_param);
                etParam.setSingleLine();
                etParam.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        onAnythingMaybeChanged();
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                }); // call onAnythingMaybeChanged if this is edited
                String paramName = paramTypes.keySet().iterator().next(); // just get that one element out of the set
                ParameterType paramType = paramTypes.get(paramName);
                if (paramType.equals(ParameterType.NUMBER)) {
                    etParam.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    etParam.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                etParam.setVisibility(View.VISIBLE);
            }
        }

        return globalRuleView;
    }

    /**
     * Adds the toggled view's rule (stored already in {@link #globalRulesM} or {@link #cardsM}
     * or removes it, depending on the passed boolean <code>useThisRule</code><br>
     * This method overwrites any previous settings for the same view (i.e. rule)
     *
     * @param view        The view which represents the rule
     * @param useThisRule true if it should add the Rule, false if it should remove it
     */
    private void onGlobalRuleToggled(View view, boolean useThisRule) {
        BasicGlobalRule rule = this.globalRulesM.get(view);
        if (rule == null) {
            Log.w("LobbySettings", "This rule View has no assicated GlobalRule instance");
        } else {
            if (useThisRule) {
                this.rulesContainer.addGlobalRule(rule);
            } else {
                this.rulesContainer.removeGlobalRule(rule);
            }
        }
    }

    /**
     * @param view The view that represents the card (with the options like number of them, what cardrules, whether to ues it, etc)
     * @param num
     */
    private void onCardNumChanged(View view, int num) {
        Card card = this.cardsM.get(view);
        if (card == null) {
            Log.w("LobbySettings/onCardNumChanged", "This rule View has no assiciated Card instance");
        } else {
            if (num <= 0) {
                this.rulesContainer.removeCard(card.getID());
            } else {
                this.rulesContainer.setNumberOfCards(card.getID(), String.valueOf(num));
            }
        }
    }

    /**
     * Turn use of Card on or off
     *
     * @param view
     * @param useThisCard
     */
    private void onCardToggled(View view, boolean useThisCard) {
        // TODO: maybe store this setting and restore it when the card is untoggled? Or maybe that is too much effort for a useless feature
        Card card = this.cardsM.get(view);
        if (card == null) {
            Log.w("LobbySettings/onCardToggled", "This rule View has no assiciated Card instance");
        } else {
            if (useThisCard) {
                this.rulesContainer.addCard(card.getID(), getNumCardsFromView(view));
            } else {
                this.rulesContainer.removeCard(card.getID());
            }
        }
    }

    /**
     * @param card
     * @param cardRuleView should have a Tag set that is of type BasicCardRule
     * @param toggledOn
     */
    private void onCardRuleToggled(Card card, View cardRuleView, boolean toggledOn) {
        BasicCardRule rule = (BasicCardRule) cardRuleView.getTag();

        if (toggledOn) {
            this.rulesContainer.addCardRule(rule, card.getID());
        } else {
            this.rulesContainer.removeCardRule(rule.getName());
        }
    }

    /**
     * @param view the view that corresponds to the card
     * @return the number of cards of this kind in the deck
     */
    private Integer getNumCardsFromView(View view) {
        return 1; // TODO: load the number from the view
    }
}
