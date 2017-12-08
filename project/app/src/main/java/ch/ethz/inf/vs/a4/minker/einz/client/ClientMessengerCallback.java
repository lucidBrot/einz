package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.UI.GameUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientMessengerCallback implements ClientActionCallbackInterface { // TODO: make sure to always cover the case where gameUI and/or lobbyUI are null
    @Nullable
    private LobbyUIInterface lobbyUI; // can be null if the corresponding Activity does not exist anymore
    @Nullable
    private GameUIInterface gameUI; // can be null if the corresponding Activity does not exist yet/anymore
    private final Context applicationContext;
    private final EinzClient parentClient;


    /**
     * @param lobbyUIInterface make sure to call {@link #setGameUIAndDisableLobbyUI(GameUIInterface)} after destroying the lobby
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
        this.gameUI = gameUI;
    }

    /**
     * Remember to set this to null again if the parameter stops existing
     */
    public void setLobbyUI(LobbyUIInterface lobbyUI){
        this.lobbyUI = lobbyUI;
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
                if(lobbyUI!=null) { // TODO: update lobby list if it changes during the game
                    lobbyUI.setAdmin(message.getBody().getAdmin());
                    lobbyUI.setLobbyList(players, spectators);
                }
            }
        };

        runOnMainThread(runnable); // this is important because
                                    // a) to access the UI, this is needed
                                    // b) to be sure the Activity still exists after checking
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
                    } else {
                        Toast.makeText(applicationContext, username+" has been disconnected. Reason: "+reason, Toast.LENGTH_LONG).show();
                    }

                    this.notifyAll(); // inform any waiting threads
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
    public void onInitGame(EinzMessage<EinzInitGameMessageBody> message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                startGameUI();
            }
        };

        runOnMainThread(runnable);
        Log.d("CliMesssegnerCallback", "Game Initialized");
        // TODO: implement onInitGame

    }

    private void startGameUI() {
        //TODO: create new GameUI and start the Game
        //actionCallbackInterface.setGameUI(initializedGameUI);
    }

    @Override
    public void onDrawCardsSuccess(EinzMessage<EinzDrawCardsSuccessMessageBody> message) {
        //nothing to do here?
        //except of course to call Chris' gameUI.onDrawCardsSuccess or maybe directly his function to update the hand
        // TODO: implement onDrawCardsSuccess
        gameUI.onDrawCardsSuccess(message);
    }

    @Override
    public void onDrawCardsFailure(EinzMessage<EinzDrawCardsFailureMessageBody> message) {
        String reason = message.getBody().getReason();
        Toast.makeText(this.applicationContext,"You're not able to draw a card because " + reason, Toast.LENGTH_SHORT).show(); // if this fails, it is because you need to run this in the main thread
        // TODO: implement onDrawCardsFailure
    }

    @Override
    public void onPlayCardResponse(EinzMessage<EinzPlayCardResponseMessageBody> message) {
        String success = message.getBody().getSuccess();
        if (!success.equals(true)){
            Toast.makeText(applicationContext, "You are not allowed to play this card", Toast.LENGTH_SHORT).show();
        }
        // TODO: implement onPlayCardResponse
    }

    @Override
    public void onSendState(EinzMessage<EinzSendStateMessageBody> message) {
        /*
        ArrayList<Card> hand = message.getBody().getPlayerState().getHand();
        ArrayList<String> actions = message.getBody().getPlayerState().getPossibleActions();

        gameUI.setHand(hand); // TODO: Chris would need to implement this

        gameUI.setActions(actions); // TODO: Chris would need to implement this


        String[] optiones = new String[5];
        */
        // TODO: implement onSendState
    }

    @Override
    public void onShowToast(EinzMessage<EinzShowToastMessageBody> message) {
        Toast.makeText(applicationContext, message.getBody().getToast(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPlayerFinished(EinzMessage<EinzPlayerFinishedMessageBody> message) {
        // TODO: implement onPlayerFinished
    }

    @Override
    public void onGameOver(EinzMessage<EinzGameOverMessageBody> message) {
        // TODO: implement onGameOver
    }

    @Override
    public void onCustomActionResponse(EinzMessage<EinzCustomActionResponseMessageBody> message) {
        // TODO: implement onCustomActionResponse
    }

    private void runOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(this.applicationContext.getMainLooper());
        mainHandler.post(runnable);
    }
}
