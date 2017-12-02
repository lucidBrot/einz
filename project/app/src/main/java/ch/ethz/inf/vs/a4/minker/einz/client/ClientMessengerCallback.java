package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientMessengerCallback implements ClientActionCallbackInterface {
    private final LobbyUIInterface lobbyUI; // TODO: implement reactions to messages
    private final Context applicationContext;
    private final EinzClient parentClient;


    public ClientMessengerCallback(LobbyUIInterface lobbyUIInterface, Context appContext, EinzClient parentClient) {
        this.lobbyUI = lobbyUIInterface;
        this.applicationContext = appContext;
        this.parentClient = parentClient;
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
                lobbyUI.setAdmin(message.getBody().getAdmin());
                lobbyUI.setLobbyList(players, spectators);
            }
        };

        runOnMainThread(runnable);
        Log.d("ClientMessengerCallback", "updated LobbyList");

    }

    @Override
    public void onRegisterFailure(EinzMessage<EinzRegisterFailureMessageBody> message) {
        Log.d("ClientMessengerCallback", "registration Failed");
        final EinzRegisterFailureMessageBody body = message.getBody();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                lobbyUI.onRegistrationFailed(body);
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

    private void runOnMainThread(Runnable runnable) {
        Handler mainHandler = new Handler(this.applicationContext.getMainLooper());
        mainHandler.post(runnable);
    }
}
