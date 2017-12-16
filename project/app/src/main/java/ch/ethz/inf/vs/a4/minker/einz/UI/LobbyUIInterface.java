package ch.ethz.inf.vs.a4.minker.einz.UI;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterResponseMessageBody;

import java.util.ArrayList;

/**
 * Will at some point offer the needed functions to update the LobbyList Activity when people join, are kicked etc
 * Feel free to edit this chris
 */
public interface LobbyUIInterface {
    public void setLobbyList(ArrayList<String> players, ArrayList<String> spectators);
    public void setAdmin(String username);

    void onRegistrationFailed(EinzRegisterFailureMessageBody body);

    void onUnregisterResponse(EinzMessage<EinzUnregisterResponseMessageBody> message);

    public void startGameUIWithThisAsContext();

    void onKeepaliveTimeout();

    // still to do:
    // on connection failed

}
