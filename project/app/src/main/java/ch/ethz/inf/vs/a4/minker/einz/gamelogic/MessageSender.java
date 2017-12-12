package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzGameOverMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.PlayerAction;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzInitGameMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSendStateMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsSuccessMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayerFinishedMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.GlobalStateParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.PlayerState;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;
import ch.ethz.inf.vs.a4.minker.einz.server.UserNotRegisteredException;

import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.cardRulesJSONHelper;
import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.drawCardsJSONHelper;
import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.finishTurnJSONHelper;
import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.globalRulesJSONHelper;
import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.kickPlayerJSONHelper;
import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.leaveGameJSONHelper;
import static ch.ethz.inf.vs.a4.minker.einz.gamelogic.JSONHelper.playCardJSONHelper;

/**
 * Created by Fabian on 05.12.2017.
 */

public class MessageSender {

    /**
     * @param tes            holds the people to send the message to
     * @param config         the configuration holding the rules
     * @param playersOrdered order in which players play
     */
    public static void sendInitGameToAll(ThreadedEinzServer tes, GameConfig config, ArrayList<Player> playersOrdered) {

        EinzMessageHeader header = new EinzMessageHeader("startgame", "InitGame");
        ArrayList<String> turnOrder = new ArrayList<>();
        for (Player p : playersOrdered) {
            turnOrder.add(p.getName());
        }
        JSONObject cardRules = cardRulesJSONHelper(config);
        JSONArray globalRules = globalRulesJSONHelper(config);
        EinzInitGameMessageBody body = new EinzInitGameMessageBody(cardRules, globalRules, turnOrder);
        EinzMessage<EinzInitGameMessageBody> message = new EinzMessage<>(header, body);
        tes.getServerManager().broadcastMessageToAllPlayers(message);
        tes.getServerManager().broadcastMessageToAllSpectators(message);
    }

    /**
     * @param p     player to send message to
     * @param tes   ThreadedEinzServer that holds the player to send the message to
     * @param cards cards the player draws if he is able to
     */
    public static void sendDrawCardResponseSuccess(Player p, ThreadedEinzServer tes, ArrayList<Card> cards) {
        EinzMessageHeader header = new EinzMessageHeader("draw", "DrawCardsSuccess");
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
     * @param p             player to send message to
     * @param tes           ThreadedEinzServer that holds the player to send the message to
     * @param failureReason reason why the player wasn't able to draw cards
     */
    public static void sendDrawCardResponseFailure(Player p, ThreadedEinzServer tes, String failureReason) {
        EinzMessageHeader header = new EinzMessageHeader("draw", "DrawCardsFailure");
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
     * @param p       player to send message to
     * @param tes     ThreadedEinzServer that holds the player to send the message to
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
     * @param tes   holds the people to send the message to
     * @param state holds the info that we need to build the messages to send
     */
    public static void sendStateToAll(ThreadedEinzServer tes, GlobalState state, GameConfig config) {
        EinzMessageHeader header = new EinzMessageHeader("stateinfo", "SendState");
        HashMap<String, String> numCardsInHand = new HashMap<>();

        //Build strings for GlobalStateParser and instantiate it
        for (Player p : state.getPlayersOrdered()) {
            numCardsInHand.put(p.getName(), Integer.toString(p.hand.size()));
        }
        ArrayList<Card> stack = new ArrayList<>(state.getDiscardPile());
        String activePlayer;
        if(state.getActivePlayer() != null) {
            activePlayer = state.getActivePlayer().getName();
        } else {
            activePlayer = "~null";
        }
        String cardsToDraw = Integer.toString(state.getCardsToDraw());
        GlobalStateParser parser = new GlobalStateParser(numCardsInHand, stack, activePlayer, cardsToDraw);

        //send each player a different PlayerState
        for (Player p : state.getPlayersOrdered()) {
            ArrayList<JSONObject> possibleActions = new ArrayList<>();

            //This loop passes the possibleActions to the auxiliary functions to add the appropriate actions as JSONObjects to it
            for (PlayerAction action : PlayerAction.values()) {
                switch (action) {
                    case LEAVE_GAME:
                        leaveGameJSONHelper(p, state, config, possibleActions);
                        break;
                    case DRAW_CARDS:
                        drawCardsJSONHelper(p, state, config, possibleActions);
                        break;
                    case KICK_PLAYER:
                        kickPlayerJSONHelper(p, tes, possibleActions);
                        break;
                    case PLAY_CARD:
                        playCardJSONHelper(p, state, config, possibleActions);
                        break;
                    case FINISH_TURN:
                        finishTurnJSONHelper(p, state, config, possibleActions);

                        break;
                    default:

                        break;
                }
            }

            //Sends the built message to the corresponding player
            try {
                PlayerState playerState = new PlayerState((ArrayList) p.hand, possibleActions);
                EinzSendStateMessageBody body = new EinzSendStateMessageBody(parser, playerState);
                EinzMessage<EinzSendStateMessageBody> message = new EinzMessage<>(header, body);
                tes.sendMessageToUser(p.getName(), message);
            } catch (UserNotRegisteredException e) {
                //ignore and continue
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        //send the spectators all the same message
        //Don't use the JSONHelper here since spectators are allways allowed to leave the game.
        ArrayList<JSONObject> possibleActions = new ArrayList<>();
        JSONObject obj = new JSONObject();
        try {
            obj.put("actionName", PlayerAction.LEAVE_GAME.name);
            obj.put("parameters", new JSONObject());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        possibleActions.add(obj);

        //Send each spectator an empty list as his "hand"
        try {
            PlayerState spectatorState = new PlayerState(new ArrayList<Card>(), possibleActions);
            EinzSendStateMessageBody body = new EinzSendStateMessageBody(parser, spectatorState);
            EinzMessage<EinzSendStateMessageBody> message = new EinzMessage<>(header, body);
            tes.getServerManager().broadcastMessageToAllSpectators(message);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


    }

    /**
     * @param p                 player to send the message to
     * @param tes               ThreadedEinzServer that holds the player to send the message to
     * @param ruleParameterBody message to send (depending on the action), including ruleName and success. If ruleName and success are not set, this throws a runtime exception
     */
    public static void sendCustomActionResponse(Player p, ThreadedEinzServer tes, JSONObject ruleParameterBody) {
        EinzMessageHeader header = new EinzMessageHeader("furtheractions", "CustomAction");
        String ruleName, success;
        try {
            ruleName = ruleParameterBody.getString("ruleName");
            success = ruleParameterBody.getString("success");
        } catch (JSONException e) {
            throw new RuntimeException(e); // You NEED to specify ruleName and success
        }
        EinzCustomActionResponseMessageBody body = new EinzCustomActionResponseMessageBody(ruleParameterBody, ruleName, success);
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
     * @param p   player that finished
     * @param tes holds the people to send the message to
     */
    public static void sendPlayerFinishedToAll(Player p, ThreadedEinzServer tes) {
        EinzMessageHeader header = new EinzMessageHeader("endgame", "PlayerFinished");
        EinzPlayerFinishedMessageBody body = new EinzPlayerFinishedMessageBody(p.getName());
        EinzMessage<EinzPlayerFinishedMessageBody> message = new EinzMessage<>(header, body);
        tes.getServerManager().broadcastMessageToAllPlayers(message);
        tes.getServerManager().broadcastMessageToAllSpectators(message);
    }

    /**
     * Points just represents order in which the players are finished
     * Players that finish first get more points
     *
     * @param state state of the game
     * @param tes   holds the players and spectators to send message to
     */
    public static void sendEndGameToAll(GlobalState state, ThreadedEinzServer tes) {
        EinzMessageHeader header = new EinzMessageHeader("endgame", "GameOver");
        /*for (int i = 0; i < state.getFinishedPlayers().size(); i++) {
            state.addPoints(state.getFinishedPlayers().get(i).getName(), state.getFinishedPlayers().size()-i);
            ///<old>/// points.put(state.getFinishedPlayers().get(i).getName(), Integer.toString(state.getFinishedPlayers().size() - i));
            // TODO move this functionality to rules
        }*/
        EinzGameOverMessageBody body = new EinzGameOverMessageBody(state.getPoints(), true);
        EinzMessage<EinzGameOverMessageBody> message = new EinzMessage<>(header, body);
        tes.getServerManager().broadcastMessageToAllPlayers(message);
        tes.getServerManager().broadcastMessageToAllSpectators(message);
    }

    public static void sendState(Player player, ThreadedEinzServer tes, GlobalState state, GameConfig config) {
        EinzMessageHeader header = new EinzMessageHeader("stateinfo", "SendState");
        HashMap<String, String> numCardsInHand = new HashMap<>();

        //Build strings for GlobalStateParser and instantiate it
        for (Player p : state.getPlayersOrdered()) {
            numCardsInHand.put(p.getName(), Integer.toString(p.hand.size()));
        }
        ArrayList<Card> stack = new ArrayList<>(state.getDiscardPile());
        String activePlayer;
        if(state.getActivePlayer() != null) {
            activePlayer = state.getActivePlayer().getName();
        } else {
            activePlayer = "~null";
        }
        String cardsToDraw = Integer.toString(state.getCardsToDraw());
        GlobalStateParser parser = new GlobalStateParser(numCardsInHand, stack, activePlayer, cardsToDraw);

        //Send State to only one player
        ArrayList<JSONObject> possibleActions = new ArrayList<>();

        //This loop passes the possibleActions to the auxiliary functions to add the appropriate actions as JSONObjects to it
        for (PlayerAction action : PlayerAction.values()) {
            switch (action) {
                case LEAVE_GAME:
                    leaveGameJSONHelper(player, state, config, possibleActions);
                    break;
                case DRAW_CARDS:
                    drawCardsJSONHelper(player, state, config, possibleActions);
                    break;
                case KICK_PLAYER:
                    kickPlayerJSONHelper(player, tes, possibleActions);
                    break;
                case PLAY_CARD:
                    playCardJSONHelper(player, state, config, possibleActions);
                    break;
                case FINISH_TURN:
                    finishTurnJSONHelper(player, state, config, possibleActions);
                    break;
                default:

                    break;
            }
        }

        //Sends the built message to the corresponding player
        try {
            PlayerState playerState = new PlayerState((ArrayList) player.hand, possibleActions);
            EinzSendStateMessageBody body = new EinzSendStateMessageBody(parser, playerState);
            EinzMessage<EinzSendStateMessageBody> message = new EinzMessage<>(header, body);
            tes.sendMessageToUser(player.getName(), message);
        } catch (UserNotRegisteredException e) {
            //ignore and continue
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
