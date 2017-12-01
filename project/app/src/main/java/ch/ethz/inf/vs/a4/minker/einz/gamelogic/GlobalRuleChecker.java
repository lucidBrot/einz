package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
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
    public boolean checkIsValidKickPlayer(GlobalState state, Player toKick, GameConfig gameConfig) {
        return false;
    }

    /**
     * Determines if a player can leave. Somehow useless because a player can still cut the connection.
     *
     * @param state
     * @param leaves The Player that wants to leave
     * @return
     */
    public boolean checkIsValidLeaveGame(GlobalState state, Player leaves, GameConfig gameConfig) {
        return false;
    }

    /**
     * Determines if the player can end his turn.
     *
     * @param state
     * @return
     */
    public boolean checkIsValidEndTurn(GlobalState state, GameConfig gameConfig) {
        return false;
    }

    /**
     * Determines if a player has finished the game and can be removed from the game cycle. This is a winning condition.
     *
     * @param state
     * @param player The Player to check if he finished
     * @return
     */
    public boolean checkIsPlayerFinished(GlobalState state, Player player, GameConfig gameConfig) {
        return false;
    }


    /**
     * Called after a player got kicked
     *
     * @param state
     * @return
     */
    public GlobalState checkOnKickPlayer(GlobalState state, GameConfig gameConfig) {
        return state;
    }

    /**
     * Called after a player leavers the game
     *
     * @param state
     * @return
     */
    public GlobalState checkOnLeaveGame(GlobalState state, GameConfig gameConfig) {
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
    public GlobalState checkOnEndTurn(GlobalState state, GameConfig gameConfig) {
        return state;
    }

    /**
     * Called after a player satisfies a winning condition
     *
     * @param state
     * @param player The Player that has finished
     * @return
     */
    public GlobalState checkOnPlayerFinished(GlobalState state, Player player, GameConfig gameConfig) {
        return state;
    }
}
