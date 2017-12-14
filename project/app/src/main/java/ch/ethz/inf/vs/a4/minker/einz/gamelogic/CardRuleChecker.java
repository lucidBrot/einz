package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.model.SelectorRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;

/**
 * Created by Fabian on 01.12.2017.
 * This Class goes through all the rules of the given type of action you want to perform in a game of EINZ.
 */

public class CardRuleChecker {

    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * In permissive mode at least one rule has to return true to allow the player to draw a card.
     * @param state
     * @return True if he is allowed to
     */
    public static boolean checkIsValidDrawCardsPermissive(GlobalState state, GameConfig gameConfig){
        boolean permissive = false;
        for (BasicRule r : gameConfig.allRules) {
            if (r instanceof BasicCardRule) {
                permissive = permissive || ((BasicCardRule) r).isValidDrawCardsPermissive(state);
            }
        }
        return permissive;
    }

    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * In restrictive mode every rule has to return true to allow the player to draw a card.
     * @param state
     * @return False if he is not allowed to.
     */
    public static boolean checkIsValidDrawCardsRestrictive(GlobalState state, GameConfig gameConfig){
        boolean restrictive = true;
        for (BasicRule r : gameConfig.allRules) {
            if (r instanceof BasicCardRule) {
                restrictive = restrictive && ((BasicCardRule) r).isValidDrawCardsRestrictive(state);
            }
        }
        return restrictive;
    }

    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * Checks rules in restrictive AND permissive.
     * @param state
     * @return False if he is not allowed to.
     */
    public static boolean checkIsValidDrawCards(GlobalState state, GameConfig gameConfig){
        return checkIsValidDrawCardsPermissive(state, gameConfig)
                && checkIsValidDrawCardsRestrictive(state, gameConfig);
    }

    /**
     * Checks whether the card that the player wants to play is valid.
     * <br />
     * In permissive mode at least one rule has to return true to allow the player to draw a card.
     * @param state
     * @param played
     * @return False if he is not allowed to.
     */
    public static boolean checkIsValidPlayCardPermissive(GlobalState state, Card played, GameConfig gameConfig){
        boolean permissive = false;
        for (BasicRule r : gameConfig.allRules) {
            if (r instanceof BasicCardRule) {
                permissive = permissive || ((BasicCardRule) r).isValidPlayCardPermissive(state, played);
            }
        }
        return permissive;
    }

    /**
     * Checks whether the card that the player wants to play is valid.
     * <br />
     * In restrictive mode every rule has to return true to allow the player to draw a card.
     * @param state
     * @param played
     * @return False if he is not allowed to.
     */
    public static boolean checkIsValidPlayCardRestrictive(GlobalState state, Card played, GameConfig gameConfig){
        boolean restrictive = true;
        for (BasicRule r : gameConfig.allRules) {
            if (r instanceof BasicCardRule) {
                restrictive = restrictive && ((BasicCardRule) r).isValidPlayCardRestrictive(state, played);
            }
        }
        return restrictive;
    }

    /**
     * Checks whether the card that the player wants to play is valid.
     * <br />
     * Checks rules in restrictive AND permissive.
     * @param state
     * @param played
     * @return False if he is not allowed to.
     */
    public static boolean checkIsValidPlayCard(GlobalState state, Card played, GameConfig gameConfig){
        return checkIsValidPlayCardPermissive(state, played, gameConfig)
                && checkIsValidPlayCardRestrictive(state, played, gameConfig);
    }





    /**
     * Called after a player decides to draw a card
     * @param state
     * @return modified state
     */
    public static GlobalState checkOnDrawCard(GlobalState state, GameConfig gameConfig){
        for (BasicRule r : gameConfig.allRules) {
            if (r instanceof BasicCardRule) {
                state = ((BasicCardRule) r).onDrawCard(state);
            }
        }
        return state;
    }

    /**
     * Called if a player played the card assigned with the rule and that rule requires a choice.
     * @param state
     * @param played
     * @return modified state
     */
    public static GlobalState checkOnPlayAssignedCardChoice(GlobalState state, Card played, GameConfig gameConfig, JSONObject playParameter) {
        for (BasicRule r: gameConfig.getRulesForCard(played)) {
            if (r instanceof BasicCardRule && ((BasicCardRule) r).getAssignedTo().getID().equals(played.getID())){
                if (r instanceof SelectorRule) {
                    JSONObject param;
                    try {
                        param = playParameter.getJSONObject(r.getName());
                    } catch (JSONException e){
                        param = null;
                    }
                    state = ((SelectorRule) r).onPlayAssignedCardChoice(state, param);
                }
            }
        }
        return state;
    }

    /**
     * Called if a player played the card assigned with the rule
     * @param state
     * @param played
     * @return modified state
     */
    public static GlobalState checkOnPlayAssignedCard(GlobalState state, Card played, GameConfig gameConfig){
        for (BasicRule r: gameConfig.getRulesForCard(played)){
            if (r instanceof BasicCardRule && ((BasicCardRule) r).getAssignedTo().getID().equals(played.getID())){
                state = ((BasicCardRule) r).onPlayAssignedCard(state, played);
            }
        }
        return state;
    }

    /**
     * Called on any card the player plays. This includes the card assigned to this rule.
     * @param state
     * @param played
     * @return modified state
     */
    public static GlobalState checkOnPlayAnyCard(GlobalState state, Card played, GameConfig gameConfig){
        for (BasicRule r: gameConfig.allRules){
            if (r instanceof BasicCardRule){
                state = ((BasicCardRule) r).onPlayAnyCard(state, played);
            }
        }
        return state;
    }

    public static GlobalState checkOnGameOver(GlobalState state, GameConfig gameConfig) {
        for (BasicRule r: gameConfig.allRules){
            if (r instanceof BasicCardRule){
                state = ((BasicCardRule) r).onGameOver(state);
            }
        }
        return state;
    }
}
