package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.UI.PlayerActivity;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.UI.GameUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import ch.ethz.inf.vs.a4.minker.einz.sensors.OrientationGetter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class ClientMessengerCallback implements ClientActionCallbackInterface { // TODO: make sure to always cover the case where gameUI and/or lobbyUI are null
    @Nullable
    private LobbyUIInterface lobbyUI; // can be null if the corresponding Activity does not exist anymore
    @Nullable
    private GameUIInterface gameUI; // can be null if the corresponding Activity does not exist yet/anymore
    private final Context applicationContext;
    private final EinzClient parentClient;
    private String previousPlayer = "~";
    private HashMap<String, JSONObject> playerSeatings = new HashMap<>();


    /**
     * @param lobbyUIInterface make sure to call {@link #setGameUIAndDisableLobbyUI(GameUIInterface)} after destroying the lobby, or at least to set LobbyUI to null
     * @param appContext just the Context of the application, for toasts and stuff
     * @param parentClient the client, duh.
     */
    public ClientMessengerCallback(LobbyUIInterface lobbyUIInterface, Context appContext, EinzClient parentClient) {
        this.gameUI = null;
        this.lobbyUI = lobbyUIInterface;
        this.applicationContext = appContext;
        this.parentClient = parentClient;
    }

    public void setGameUI(GameUIInterface gameUI){
        if(gameUI!=null){
            Log.d("ClientMessengerCallback", "set GameUI to " + gameUI.toString() + "    (LobbyUI : " +
                    (lobbyUI != null ? lobbyUI.toString() : "null") + ")");
        }
        this.gameUI = gameUI;
    }

    /**
     * Remember to set this to null again if the parameter stops existing
     */
    public void setLobbyUI(LobbyUIInterface lobbyUI){
        this.lobbyUI = lobbyUI;
        Log.d("ClientMessengerCallback", "set lobbyUI to "+(lobbyUI==null?"null":lobbyUI.toString()));
    }

    /**
     * set {@link #lobbyUI} to null and {@link #gameUI} to the parameter.
     * @param gameUI The Activity implementing the {@link GameUIInterface}
     */
    public void setGameUIAndDisableLobbyUI(GameUIInterface gameUI){
        setGameUI(gameUI);
        setLobbyUI(null);
    }

    public LobbyUIInterface getLobbyUI() {
        return lobbyUI;
    }

    public void onKeepaliveTimeout(){
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(applicationContext, "FUCK I LOST CONNECTION", Toast.LENGTH_LONG).show();
                if(lobbyUI!=null){
                    lobbyUI.onKeepaliveTimeout();
                }
                if(gameUI!=null){
                    gameUI.onKeepaliveTimeout();
                }
            }
        });
    }

    public GameUIInterface getGameUI() {
        return gameUI;
    }

    @Override
    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message) {
        Log.d("ClientMessengerCallback", "received RegisterSuccess");
    }

    @Override
    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message_) {
        Log.d("ClientMessengerCallback", "received UpdateLobbyList");
        final ArrayList<String> players = new ArrayList<>();
        final ArrayList<String> spectators = new ArrayList<>();
        final EinzMessage<EinzUpdateLobbyListMessageBody> message = message_;
        EinzUpdateLobbyListMessageBody body = message.getBody();
        HashMap<String, String> hashMap = body.getLobbylist();

        // this code uses JAVA8 and crashes on API 23. Works on API 26. (both emulators with Nexus)
        /*
        hashMap.forEach((username, s2) -> {
            if(s2.equals("spectator")){
                spectators.add(username);
            } else if (s2.equals("player")){
                players.add(username);
            }
        });
        */

        // same functionality but works on older devices:
        for (String username : hashMap.keySet()) {
            String s2 = hashMap.get(username);
            if (s2.equals("spectator")) {
                spectators.add(username);
            } else if (s2.equals("player")) {
                players.add(username);
            }
        }

        // only the thread that created the view is allowed to update them
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(lobbyUI!=null) {
                    lobbyUI.setAdmin(message.getBody().getAdmin());
                    lobbyUI.setLobbyList(players, spectators);
                }

                if(gameUI!=null){
                    //gameUI.onUpdateLobbyList(message.getBody().getAdmin(), players, spectators);
                    gameUI.onUpdateLobbyList(message); // FIXME: seems not to be called on spectator
                }
            }
        };

        runOnMainThread(runnable); // this is important because
                                    // a) to access the UI, this is needed
                                    // b) to be sure the Activity still exists after checking

        // parse and store the orientation
        this.playerSeatings = message.getBody().getPlayerSeatings();
        Log.d("ClientMessengerCallback", "updated LobbyList");

    }

    @Override
    public void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message) {
        Log.d("ClientMessengerCallback", "registration Failed");
        final EinzRegisterFailureMessageBody body = message.getBody();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(lobbyUI!=null) { // ignore the incoming registerFailure message if we're already in the game phase... That could only happen if we were registered, then unregistered during the game and tried to reregister but failed...
                    lobbyUI.onRegistrationFailed(body);
                }
            }
        };

        runOnMainThread(runnable);

    }

    @Override
    public void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message){
        EinzUnregisterResponseMessageBody body = message.getBody();
        final String username = body.getUsername();
        final String reason = body.getReason();

        Log.d("ClientMessengerCallback", username+" was unregistered. Reason: "+reason);
        // UI is updated later when we receive a LobbyListUpdatedMessage
        // except for when the server shuts down maybe

        // if I was kicked or left, stop the client.
        // run on main thread but wait for this to finish
        Runnable r = new Runnable() {
            @Override
            public void run() {
                synchronized(this) { // for notifyAll
                    if(username.equals(parentClient.getUsername())){
                        parentClient.shutdown(false);
                        Toast.makeText(applicationContext, "You have been disconnected. Reason: "+reason, Toast.LENGTH_LONG).show();
                        onKeepaliveTimeout(); // TODO: give this a less toasty feeling

                    } else {
                        Toast.makeText(applicationContext, username+" has been disconnected. Reason: "+reason, Toast.LENGTH_LONG).show();
                    }

                    /*r.*/notifyAll(); // inform any waiting threads
                }
            }
        };
        runOnMainThread(r);
        synchronized (r) { // synchronized needed for wait
            try {
                r.wait(1000); // wait until the main thread is finished and informs us. or until it took it 1 second
                // might spuriously awaken, but because the whole runnable is synchronized, that shouldn't be a problem
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.w("ClientMessengerCallback", "Was interrupted while waiting for Toast.show() on unregister event");
            }
        }

        // TODO: notify user either now or on updatelobbylist that somebody left and why (maybe not with a toast, or is toast fine?)

    }

    @Override
    public void onKickFailure(EinzMessage<EinzKickFailureMessageBody> message) {
        // TODO: implement this method, probably add a function onKickFailure to LobbyUI Interface and the game-in-progress UI
        Log.d("CliMesssegnerCallback", "onKickFailure");
    }

    @Override
    public void onInitGame(final EinzMessage<EinzInitGameMessageBody> message) {

        if (gameUI==null && lobbyUI!=null) { // @Clemens I completely rewrote this section. I hope you're fine with this
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    lobbyUI.startGameUIWithThisAsContext();
                    // this sets gameUI, so we can now do the following:
                }
            });

            while(gameUI==null){ // sleep until the activity has loaded, then initGame
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    gameUI.onInitGame(message);
                }
            };
            runOnMainThread(runnable);

        }else if(gameUI!=null){ // for some reason, it is already running. Reinitialize. TODO: does that make sense?

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    gameUI.onInitGame(message);
                }
            };

            runOnMainThread(runnable);

        } else {
            // lobbyUI == null and gameUI == null
            // uhm... that means that neither is currently existing. maybe, the lobbyUI has been paused.
            /*
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    lobbyUI.startGameUIWithThisAsContext();
                    // this sets gameUI, asynchronously to our message-receiver thread
                }
            });

            while(gameUI==null){ // sleep until the activity has loaded, then initGame
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    gameUI.onInitGame(message);
                }
            });*/
            Log.e("ClientMessengerCallback","Didn't have an activity that could have handled onInitGame");
        }

        Log.d("CliMesssengerCallback", "Game Initialized");
        // TODO: implement onInitGame

    }


    @Override
    public void onDrawCardsSuccess(EinzMessage<EinzDrawCardsSuccessMessageBody> message) {
        //nothing to do here?
        //except of course to call Chris' gameUI.onDrawCardsSuccess or maybe directly his function to update the hand
        // TODO: implement onDrawCardsSuccess

        final EinzMessage<EinzDrawCardsSuccessMessageBody> msg = message;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EinzMessage<EinzDrawCardsSuccessMessageBody> msg2 =msg;
                gameUI.onDrawCardsSuccess(msg2);
            }
        };

        runOnMainThread(runnable);
    }

    @Override
    public void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message) {
        final String reason = message.getBody().getReason();
        final EinzMessage<EinzDrawCardsFailureMessageBody> msg = message;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(applicationContext,"You're not able to draw a card because " + reason, Toast.LENGTH_SHORT).show();
                EinzMessage<EinzDrawCardsFailureMessageBody> msg2 =msg;
                gameUI.onDrawCardsFailure(msg2);
            }
        };

        runOnMainThread(runnable);

        // TODO: implement onDrawCardsFailure
    }

    @Override
    public void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message) {
        final EinzMessage<EinzPlayCardResponseMessageBody> msg = message;
        String success = message.getBody().getSuccess();
        if (!success.equals("true")){
            runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(applicationContext, "You are not allowed to play this card", Toast.LENGTH_SHORT).show();
                }
            });
        }

        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                gameUI.onPlayCardResponse(msg);
            }
        });
    }

    @Override
    public void onSendState(final EinzMessage<EinzSendStateMessageBody> message) {
        
        final ArrayList<Card> handtemp = message.getBody().getPlayerState().getHand();
        final ArrayList<String> actionstemp = message.getBody().getPlayerState().getPossibleActionsNames();
        final HashMap<String,String> numCardsInHandOfEachPlayer = message.getBody().getGlobalstate().getNumCardsInHand();

        final ArrayList<Card> currStack = message.getBody().getGlobalstate().getStack();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(gameUI!=null) {
                    ArrayList<Card> hand = handtemp;
                    ArrayList<String> actions = actionstemp;

                    gameUI.setHand(hand);
                    gameUI.setActions(actions);
                    gameUI.setNumCardsInHandOfEachPlayer(numCardsInHandOfEachPlayer);

                    gameUI.setStack(currStack);

                    String whoseCurrentTurn = message.getBody().getGlobalstate().getWhoseTurn();
                    if(!whoseCurrentTurn.equals(previousPlayer)){
                        gameUI.playerStartedTurn(whoseCurrentTurn);
                        previousPlayer = whoseCurrentTurn;
                    }
                }
            }
        };

        runOnMainThread(runnable);

    }

    @Override
    public void onShowToast(final EinzMessage<EinzShowToastMessageBody> message) {
        runOnMainThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(applicationContext, message.getBody().getToast(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message) {

        final EinzMessage<EinzPlayerFinishedMessageBody> msg = message;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EinzMessage<EinzPlayerFinishedMessageBody> msg2 = msg;
                gameUI.onPlayerFinished(msg2);
            }
        };

        runOnMainThread(runnable);

        // TODO: implement onPlayerFinished
    }

    @Override
    public void onGameOver(EinzMessage<EinzGameOverMessageBody> message) {

        final EinzMessage<EinzGameOverMessageBody> msg = message;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EinzMessage<EinzGameOverMessageBody> msg2 = msg;
                gameUI.onGameOver(msg2);
            }
        };

        runOnMainThread(runnable);

        // TODO: implement onGameOver
    }

    @Override
    public void onCustomActionResponse(EinzMessage<EinzCustomActionResponseMessageBody> message) {
        final EinzMessage<EinzCustomActionResponseMessageBody> msg = message;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EinzMessage<EinzCustomActionResponseMessageBody> msg2 = msg;
                gameUI.onCustomActionResponse(msg2);
            }
        };
        // TODO: implement onCustomActionResponse
    }

    private void runOnMainThread(final Runnable runnable) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(gameUI==null){
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } // wait for gameUI to exist, then perform the action
                runnable.run();
            }
        });
        Handler mainHandler = new Handler(this.applicationContext.getMainLooper());
        mainHandler.post(runnable);
    }

    public HashMap<String, JSONObject> getPlayerSeatings() {
        return playerSeatings;
    }

    /**
     * Retries 3 times to send that message, but that should not need to be done actually, because the client will be ready when this message is sent.
     * @param cardRules a list of JSONObjects consisting of attributes as defined in the docsumentation:
     *                  A String id and a JSONObject parameters, as well as a number that says how many cards of this type should be in the deck
     * @param globalRules an Array of JSONObjects, consisting of a number and a ruleslist
     *                    <br><br>
     *
     *                     "cardRules":{
    "someCardID":{
    "rulelist":[
    {"id":"instantWinOnCardPlayed", "parameters":{}},
    {"id":"chooseColorCard", "parameters":{"param1":"lulz",
    "lolcat":"foobar"}}
    ],
    "number":"3"
    },
    "otherCardID":{
    "number":"7",
    "rulelist":[
    {"id":"instantWinOnCardPlayed", "parameters":{}}
    ]
    }
     */
    public void sendSpecifyRules(JSONObject cardRules, JSONArray globalRules){
        EinzMessageHeader header = new EinzMessageHeader("startgame", "SpecifyRules");
        EinzSpecifyRulesMessageBody body = new EinzSpecifyRulesMessageBody(cardRules, globalRules);
        EinzMessage<EinzSpecifyRulesMessageBody> message = new EinzMessage<>(header, body);
        this.parentClient.getConnection().sendMessageRetryXTimes(3,message);
    }

}
