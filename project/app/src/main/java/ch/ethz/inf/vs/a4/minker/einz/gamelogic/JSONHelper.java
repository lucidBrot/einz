package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.PlayerAction;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;

/**
 * Created by Fabian on 08.12.2017.
 */

public class JSONHelper {

    /**
     * this is used to add the appropriate JSONObject to the list of possible actions for a certain player
     *
     * @param p               player that wants to have the possibility (action) to play cards
     * @param state           current globalState
     * @param config          configuration of the game
     * @param possibleActions List of possible actions that the playCardAction should be added to if the player can play at least one card
     */
    public static void playCardJSONHelper(Player p, GlobalState state, GameConfig config, ArrayList<JSONObject> possibleActions) {
        if (!p.equals(state.getActivePlayer())) {
            return; //TODO: Check in rules whether it is a players turn
        }
        JSONArray playableCards = new JSONArray();
        for (Card c : PlayerActionChecker.playableCards(p, state, config)) {
            playableCards.put(c.getID());
        }
        if (playableCards.length() > 0) {
            JSONObject playCardAction = new JSONObject();
            try {
                playCardAction.put("actionName", PlayerAction.PLAY_CARD.name);
                playCardAction.put("parameters", playableCards);
                possibleActions.add(playCardAction);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void leaveGameJSONHelper(Player p, GlobalState state, GameConfig config, ArrayList<JSONObject> possibleActions) {
        if (GlobalRuleChecker.checkIsValidLeaveGame(state, p, config)) {
            JSONObject leaveAction = new JSONObject();
            try {
                leaveAction.put("actionName", PlayerAction.LEAVE_GAME.name);
                leaveAction.put("parameters", new JSONObject());
                possibleActions.add(leaveAction);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void drawCardsJSONHelper(Player p, GlobalState state, GameConfig config, ArrayList<JSONObject> possibleActions) {
        if (!p.equals(state.getActivePlayer())) {
            return; //TODO: Check in rules whether it is a players turn
        }
        if (CardRuleChecker.checkIsValidDrawCards(state, config)) {
            JSONObject drawAction = new JSONObject();
            try {
                drawAction.put("actionName", PlayerAction.DRAW_CARDS.name);
                drawAction.put("parameters", new JSONObject());
                possibleActions.add(drawAction);
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }
    }

    public static void kickPlayerJSONHelper(Player p, ThreadedEinzServer tes, ArrayList<JSONObject> possibleActions) {
        if (p.equals(tes.getServerManager().getAdminUsername())) {
            JSONObject kickPlayerAction = new JSONObject();
            try {
                kickPlayerAction.put("actionName", PlayerAction.KICK_PLAYER.name);
                kickPlayerAction.put("parameters", new JSONObject());
                possibleActions.add(kickPlayerAction);
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }
    }

    public static void finishTurnJSONHelper(Player p, GlobalState state, GameConfig config, ArrayList<JSONObject> possibleActions) {
        if (!p.equals(state.getActivePlayer())) {
            return; //TODO: Check in rules whether it is a players turn
        }
        if (GlobalRuleChecker.checkIsValidEndTurn(state, p, config)) {
            JSONObject finishTurnAction = new JSONObject();
            try {
                finishTurnAction.put("actionName", PlayerAction.FINISH_TURN.name);
                finishTurnAction.put("parameters", new JSONObject());
                possibleActions.add(finishTurnAction);
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }
    }

    public static JSONObject cardRulesJSONHelper(GameConfig config) {

        JSONObject cardRules = new JSONObject();
        for (Card c : config.allCardsInGame) {
            JSONArray specificRules = new JSONArray();
            for (BasicRule r : config.allRules) {
                if (r instanceof BasicCardRule && ((BasicCardRule) r).getAssignedTo().equals(c)) {
                    JSONObject specificRule = new JSONObject();
                    try {
                        specificRule.put(((BasicCardRule) r).getAssignedTo().getID(), r.getName());
                        if(r instanceof ParametrizedRule) {
                            specificRule.put("parameters", ((ParametrizedRule) r).getParameterTypes());
                        } else {
                            specificRule.put("parameters", new JSONObject());
                        }
                        specificRules.put(specificRule);
                    } catch (JSONException e) {
                        throw new RuntimeException();
                    }
                }
            }
            try {
                cardRules.put(c.getID(), specificRules);
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }
        return cardRules;
    }

    public static JSONArray globalRulesJSONHelper(GameConfig config) {
        JSONArray result = new JSONArray();
        for (BasicGlobalRule r : config.globalRules) {
            JSONObject oneRule = new JSONObject();
            try {
                oneRule.put(r.getName(), r.getName()); //TODO: Currently name is used as ID
                if (r instanceof ParametrizedRule){
                    oneRule.put("parameters", ((ParametrizedRule) r).getParameterTypes());
                } else {
                    oneRule.put("parameters", new JSONObject());
                }
                result.put(oneRule);
                //If rules have ID put ,them here, otherwise dont
            } catch (JSONException e) {
                throw new RuntimeException();
            }
        }
        return result;
    }
}
