//package ch.ethz.inf.vs.a4.minker.einz.gamelogic;
//
//import android.util.Log;
//import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
//import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
//import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
//import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionMessageBody;
//import ch.ethz.inf.vs.a4.minker.einz.model.ClientCallbackService;
//import ch.ethz.inf.vs.a4.minker.einz.model.Player;
//import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;
//import ch.ethz.inf.vs.a4.minker.einz.server.UserNotRegisteredException;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.List;
//
//public class ClientCallbackFromRules implements ClientCallbackService {
//
//    // -------------------------- The ClientCallbackService ----------------------------
//    // ClientCallbackService was created by Josua to draw a line between the model and the implementation.
//    // It is used by rules to send messages back to the server, so I thought this would fit best here.
//    // Reasons:
//    //  - ThreadedEinzServer only cares about the connection itself, and the usernames associated with them
//    //  - ServerManager only cares about the things that belong neither into the gamelogic nor into the ThreadedEinzServer
//    //  - The MessageSender class is probably already easily available within the gamelogic, but it is not intended for receiving messages
//
//    private final ThreadedEinzServer server;
//
//    public ClientCallbackFromRules(ThreadedEinzServer server){
//        this.server = server;
//    }
//
//    /**
//     * Sends the options List in a <code>{"list":[]}</code> format.<br>
//     *     Super description:<br>
//     * {@inheritDoc}
//     * @param ruleName A identifier for the rule
//     * @param player   The player who has to choose
//     * @param options  The options the player can choose from
//     * @return         The response String, already parsed to only be just that.<br>
//     *     <code>null</code> if the Player was not registered
//     */
//    @Override
//    public String getSelectionFromPlayer(String ruleName, Player player, List<String> options) {
//        // format options to json parameter
//        JSONObject params = new JSONObject();
//        try {
//            params.put("list", options);
//        } catch (JSONException e) {
//            throw new RuntimeException(e); // this shouldn't be possible to happen
//        }
//        // Build message & send it
//        try {
//            server.sendMessageToUser(player.getName(), buildMessage("furtheractions", "CustomAction", params));
//        } catch (UserNotRegisteredException e) {
//            Log.w("ClientCallbackFromRules", "User "+player.getName()+" was not registered.");
//            return null;
//        } catch (JSONException e) {
//            throw new RuntimeException(e); // this shouldn't be possible to happen
//        }
//        // wait for response somehow
//
//        // Return response
//    }
//
//    private EinzMessage<EinzCustomActionMessageBody> buildMessage (String messagegroup, String messagetype, JSONObject parameters){
//        // Build message
//        EinzMessageHeader header = new EinzMessageHeader(messagegroup, messagetype);
//        EinzCustomActionMessageBody body = new EinzCustomActionMessageBody(parameters);
//        return new EinzMessage<EinzCustomActionMessageBody> (header, body);
//    }
//}
