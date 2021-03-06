package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;

/**
 * Created by Fabian on 01.12.2017.
 */

public class GlobalRuleChecker {
    /**
     * Determines if a player can be kicked
     * Currently applies isValidKickPlayer permissive
     *
     * @param state
     * @param toKick The Player that should be kicked
     * @return whether the player is kickable
     */
    public static boolean checkIsValidKickPlayer(GlobalState state, Player toKick, GameConfig gameConfig) {
        boolean permissive = false;
        for (BasicGlobalRule r : gameConfig.globalRules) {
            permissive = permissive || r.isValidKickPlayer(state, toKick);
        }
        return permissive;
    }

    /**
     * Determines if a player can leave. Somehow useless because a player can still cut the connection.
     * Currently applies isValidLeaveGame permissive
     *
     * @param state
     * @param leaves The Player that wants to leave
     * @return
     */
    public static boolean checkIsValidLeaveGame(GlobalState state, Player leaves, GameConfig gameConfig) {
        boolean permissive = false;
        for (BasicGlobalRule r : gameConfig.globalRules) {
            permissive = permissive || r.isValidLeaveGame(state, leaves);
        }
        return permissive;
    }

    /**
     * Determines if the player can end his turn.
     *Currently applies isValidEndTurn permissive
     *
     * @param state
     * @return whether a player can en his turn
     */
    public static boolean checkIsValidEndTurn(GlobalState state, Player player, GameConfig gameConfig) {
        boolean permissive = false;
        for (BasicGlobalRule r : gameConfig.globalRules) {
            permissive = permissive || r.isValidLeaveGame(state, player);
        }
        return permissive;
    }

    /**
     * Determines if a player has finished the game and can be removed from the game cycle. This is a winning condition.
     * Applies the isPlayerFinishedRules permissive
     *
     * @param state
     * @param player The Player to check if he finished
     * @return
     */
    public static boolean checkIsPlayerFinished(GlobalState state, Player player, GameConfig gameConfig) {
        boolean permissive = false;
        for (BasicGlobalRule r : gameConfig.globalRules) {
            permissive = permissive || r.isPlayerFinished(state, player);
        }
        return permissive;
    }


    /**
     * Called after a player got kicked
     *
     * @param state
     * @return
     */
    public static GlobalState checkOnKickPlayer(GlobalState state, GameConfig gameConfig) {
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onKickPlayer(state);
        }
        return state;
    }

    /**
     * Called after a player leaves the game
     *
     * @param state
     * @return modified state
     */
    public static GlobalState checkOnLeaveGame(GlobalState state, GameConfig gameConfig) {
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onLeaveGame(state);
        }
        return state;
    }

    /**
     * Called before the game starts to setup the game state.
     *
     * @param state
     * @return modified state
     */
    public static GlobalState checkOnStartGame(GlobalState state, GameConfig gameConfig) {
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onStartGame(state);
        }
        return state;
    }

    /**
     * Called after a player ended his turn.
     *
     * @param state
     * @return modified state
     */
    public static GlobalState checkOnEndTurn(GlobalState state, GameConfig gameConfig) {
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onEndTurn(state);
        }
        return state;
    }

    /**
     * Called after a player satisfies a winning condition
     *
     * @param state
     * @param player The Player that has finished
     * @return modified state
     */
    public static GlobalState checkOnPlayerFinished(GlobalState state, Player player, GameConfig gameConfig) {
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onPlayerFinished(state, player);
        }
        return state;
    }

    /**
     * Called after a card player decides to draw a card
     * @param state
     * @return modified state
     */
    public static GlobalState checkOnDrawCard(GlobalState state, GameConfig gameConfig){
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onDrawCard(state);
        }
        return state;
    }

    /**
     * Called on any card the player plays.
     * @param state
     * @param played
     * @return modified state
     */
    public static GlobalState checkOnPlayAnyCard(GlobalState state, Card played, GameConfig gameConfig){
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onPlayAnyCard(state, played);
        }
        return state;
    }

    /**
     * called when the game is over, e.g. to determine the ranking points
     * @param state
     * @param gameConfig
     * @return modified state
     */
    public static GlobalState checkOnGameOver(GlobalState state, GameConfig gameConfig){
        for (BasicGlobalRule r : gameConfig.globalRules){
            state = r.onGameOver(state);
        }
        return state;
    }
}
