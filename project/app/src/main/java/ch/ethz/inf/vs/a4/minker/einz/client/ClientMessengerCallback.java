package ch.ethz.inf.vs.a4.minker.einz.client;

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

    public ClientMessengerCallback(LobbyUIInterface lobbyUIInterface){
        this.lobbyUI = lobbyUIInterface;
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

        hashMap.forEach(new BiConsumer<String, String>() {
            @Override
            public void accept(String username, String s2) {
                if(s2.equals("spectator")){
                    spectators.add(username);
                } else if (s2.equals("player")){
                    players.add(username);
                }
            }
        });

        this.lobbyUI.setAdmin(message.getBody().getAdmin());
        this.lobbyUI.setLobbyList(players, spectators);
    }
}
