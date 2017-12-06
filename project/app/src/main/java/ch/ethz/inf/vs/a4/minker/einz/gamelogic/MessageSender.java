package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.PlayerAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzInitGameMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayerFinishedMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSendStateMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.GlobalStateParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.PlayerState;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;
import ch.ethz.inf.vs.a4.minker.einz.server.UserNotRegisteredException;

/**
 * Created by Fabian on 05.12.2017.
 */

public class MessageSender {

    /**
     * @param tes holds the people to send the message to
     * @param ruleSet rules to broadcast to the clients
     * @param playersOrdered order in which players play
     */
    public static void sendInitGameToAll(ThreadedEinzServer tes, ArrayList<BasicRule> ruleSet, ArrayList<Player> playersOrdered){

        EinzMessageHeader header = new EinzMessageHeader("startgame", "InitGame");
        ArrayList<String> turnOrder = new ArrayList<>();
        for (Player p: playersOrdered){
            turnOrder.add(p.getName());
        }
        EinzInitGameMessageBody body = new EinzInitGameMessageBody(ruleSet, turnOrder);
        EinzMessage<EinzInitGameMessageBody> message = new EinzMessage<>(header, body);
        tes.getServerManager().broadcastMessageToAllPlayers(message);
        tes.getServerManager().broadcastMessageToAllSpectators(message);

    }

    /**
     * @param p player to send message to
     * @param tes ThreadedEinzServer that holds the player to send the message to
     * @param cards cards the player draws if he is able to
     */
    public static void sendDrawCardResponseSuccess(Player p, ThreadedEinzServer tes, ArrayList<Card> cards){
        EinzMessageHeader header = new EinzMessageHeader("draw", "DrawCardsResponse");
        EinzDrawCardsSuccessMessageBody body = new EinzDrawCardsSuccessMessageBody(cards);
        EinzMessage<EinzDrawCardsSuccessMessageBody> message = new EinzMessage<>(header, body);
        try {
            tes.sendMessageToUser(p.getName(), message);
        } catch (UserNotRegisteredException e) {
            //ignore and continue
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param p player to send message to
     * @param tes ThreadedEinzServer that holds the player to send the message to
     * @param failureReason reason why the player wasn't able to draw cards
     */
    public static void sendDrawCardResponseFailure(Player p, ThreadedEinzServer tes, String failureReason){
        EinzMessageHeader header = new EinzMessageHeader("draw", "DrawCardsResponse");
        EinzDrawCardsFailureMessageBody body = new EinzDrawCardsFailureMessageBody(failureReason);
        EinzMessage<EinzDrawCardsFailureMessageBody> message = new EinzMessage<>(header, body);
        try {
            tes.sendMessageToUser(p.getName(), message);
        } catch (UserNotRegisteredException e) {
            //ignore and continue
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param p player to send message to
     * @param tes ThreadedEinzServer that holds the player to send the message to
     * @param success Whether the card was played or not
     */
    public static void sendPlayCardResponse(Player p, ThreadedEinzServer tes, boolean success) {
        EinzMessageHeader header = new EinzMessageHeader("playcard", "PlayCardResponse");
        EinzPlayCardResponseMessageBody body = new EinzPlayCardResponseMessageBody(Boolean.toString(success));
        EinzMessage<EinzPlayCardResponseMessageBody> message = new EinzMessage<>(header, body);
        try {
            tes.sendMessageToUser(p.getName(), message);
        } catch (UserNotRegisteredException e) {
            //ignore and continue
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param tes holds the people to send the message to
     * @param state holds the info that we need to build the messages to send
     */
    public static void sendStateToAll(ThreadedEinzServer tes, GlobalState state){
        EinzMessageHeader header = new EinzMessageHeader("stateinfo", "SendState");
        HashMap<String, String> numCardsInHand = new HashMap<>();

        //Build strings for GlobalStateParser and instatiatae it
        for (Player p: state.getPlayersOrdered()){
            numCardsInHand.put(p.getName(), Integer.toString(p.hand.size()));
        }
        ArrayList<Card> stack = (ArrayList) state.getDiscardPile();
        String activePlayer = state.getActivePlayer().toString();
        String cardsToDraw = Integer.toString(state.getCardsToDraw());
        GlobalStateParser parser = new GlobalStateParser(numCardsInHand, stack, activePlayer, cardsToDraw);

        //send each player a different PlayerState
        for (Player p: state.getPlayersOrdered()){

            //TODO: Test which actions a player can actually do
            ArrayList<String> possibleActions = new ArrayList<>();
            for (PlayerAction action: PlayerAction.values()){
                possibleActions.add(action.name);
            }

            PlayerState playerState = new PlayerState((ArrayList) p.hand, possibleActions);
            EinzSendStateMessageBody body = new EinzSendStateMessageBody(parser, playerState);
            EinzMessage<EinzSendStateMessageBody> message = new EinzMessage<>(header, body);
            try {
                tes.sendMessageToUser(p.getName(), message);
            } catch (UserNotRegisteredException e) {
                //ignore and continue
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        //send the spectators all the same message
        ArrayList<String> possibleActions = new ArrayList<>();
        possibleActions.add(PlayerAction.LEAVE_GAME.name);
        PlayerState spectatorState = new PlayerState(new ArrayList<Card>(), possibleActions); //Send each spectator an empty list as his "hand"
        EinzSendStateMessageBody body = new EinzSendStateMessageBody(parser, spectatorState);
        EinzMessage<EinzSendStateMessageBody> message = new EinzMessage<>(header, body);
        tes.getServerManager().broadcastMessageToAllSpectators(message);

    }

    /**
     * @param p player to send the message to
     * @param tes ThreadedEinzServer that holds the player to send the message to
     * @param ruleParameter message to send (depending on the action)
     */
    public static void sendCustomActionResponse(Player p, ThreadedEinzServer tes, JSONObject ruleParameter){
        EinzMessageHeader header = new EinzMessageHeader("furtheractions", "CustomAction");
        EinzCustomActionResponseMessageBody body = new EinzCustomActionResponseMessageBody(ruleParameter);
        EinzMessage<EinzCustomActionResponseMessageBody> message = new EinzMessage<>(header, body);
        try {
            tes.sendMessageToUser(p.getName(), message);
        } catch (UserNotRegisteredException e) {
            //ignore and continue
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param p player that finished
     * @param tes holds the people to send the message to
     */
    public static void sendPlayerFinishedToAll(Player p, ThreadedEinzServer tes){
        EinzMessageHeader header = new EinzMessageHeader("endgame", "PlayerFinished");
        EinzPlayerFinishedMessageBody body = new EinzPlayerFinishedMessageBody(p.getName());
        EinzMessage<EinzPlayerFinishedMessageBody> message = new EinzMessage<>(header, body);
        tes.getServerManager().broadcastMessageToAllPlayers(message);
        tes.getServerManager().broadcastMessageToAllSpectators(message);
    }

}
