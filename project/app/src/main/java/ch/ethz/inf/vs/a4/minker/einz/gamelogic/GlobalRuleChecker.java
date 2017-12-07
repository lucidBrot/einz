package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import ch.ethz.inf.vs.a4.minker.einz.rules.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.rules.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Fabian on 01.12.2017.
 */

public class GlobalRuleChecker {
    /**
     * Determines if a player can be kicked
     *
     * @param state
     * @param toKick The Player that should be kicked
     * @return
     */
    public static boolean checkIsValidKickPlayer(GlobalState state, Player toKick, GameConfig gameConfig) {
        return false;
    }

    /**
     * Determines if a player can leave. Somehow useless because a player can still cut the connection.
     *
     * @param state
     * @param leaves The Player that wants to leave
     * @return
     */
    public static boolean checkIsValidLeaveGame(GlobalState state, Player leaves, GameConfig gameConfig) {
        return false;
    }

    /**
     * Determines if the player can end his turn.
     *
     * @param state
     * @return
     */
    public static boolean checkIsValidEndTurn(GlobalState state, GameConfig gameConfig) {
        return false;
    }

    /**
     * Determines if a player has finished the game and can be removed from the game cycle. This is a winning condition.
     *
     * @param state
     * @param player The Player to check if he finished
     * @return
     */
    public static boolean checkIsPlayerFinished(GlobalState state, Player player, GameConfig gameConfig) {
        return false;
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
     * Called after a player leavers the game
     *
     * @param state
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
     */
    public static GlobalState checkOnPlayAnyCard(GlobalState state, Card played, GameConfig gameConfig){
        for (BasicGlobalRule r : gameConfig.globalRules) {
            state = r.onPlayAnyCard(state, played);
        }
        return state;
    }
}
