package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Spectator;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUpdateLobbyListMessageBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class ClientMessengerCallback implements ClientActionCallbackInterface {
    private final LobbyUIInterface lobbyUI; // TODO: implement reactions to messages
    private final Context applicationContext;


    public ClientMessengerCallback(LobbyUIInterface lobbyUIInterface, Context appContext){
        this.lobbyUI = lobbyUIInterface;
        this.applicationContext=appContext;
    }

    @Override
    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message) {
        Log.d("ClientMessengerCallback", "received RegisterSuccess");
    }

    @Override
    public void onUpdateLobbyList(EinzMessage<EinzUpdateLobbyListMessageBody> message) {
        Log.d("ClientMessengerCallback", "received UpdateLobbyList");
        final ArrayList<String> players = new ArrayList<>();
        final ArrayList<String> spectators = new ArrayList<>();
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
        for(String username : hashMap.keySet()){
            String s2 = hashMap.get(username);
            if(s2.equals("spectator")){
                spectators.add(username);
            } else if (s2.equals("player")){
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

    private void runOnMainThread(Runnable runnable){
        Handler mainHandler = new Handler(this.applicationContext.getMainLooper());
        mainHandler.post(runnable);
    }
}
