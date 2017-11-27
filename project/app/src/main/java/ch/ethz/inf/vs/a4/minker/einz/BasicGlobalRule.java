package ch.ethz.inf.vs.a4.minker.einz;


/**
 * Basic rule class for rules not applicable to cards.
 *
 * Example:
 *      Determine number of cards in beginning of a game.
 *      Determine how often a card is in the deck
 *      Determine when a player wins.
 */
public abstract class BasicGlobalRule extends BasicRule {

    public BasicGlobalRule(GameConfig config) {
        super(config);
    }

    /**
     * Determines if a player can be kicked
     * @param state
     * @param toKick The Player that should be kicked
     * @return
     */
    public boolean isValidKickPlayer(GlobalState state, Player toKick) {
        return false;
    }

    /**
     * Determines if a player can leave. Somehow useless because a player can still cut the connection.
     * @param state
     * @param leaves The Player that wants to leave
     * @return
     */
    public boolean isValidLeaveGame(GlobalState state, Player leaves){
        return false;
    }

    /**
     * Determines if the player can end his turn.
     * @param state
     * @return
     */
    public boolean isValidEndTurn(GlobalState state) {
        return false;
    }

    /**
     * Determines if a player has finished the game and can be removed from the game cycle. This is a winning condition.
     * @param state
     * @param player The Player to check if he finished
     * @return
     */
    public boolean isPlayerFinished(GlobalState state, Player player) {
        return false;
    }




    /**
     * Called after a player got kicked
     * @param state
     * @return
     */
    public GlobalState onKickPlayer(GlobalState state){
        return state;
    }

    /**
     * Called after a player leavers the game
     * @param state
     * @return
     */
    public GlobalState onLeaveGame(GlobalState state){
        return state;
    }

    /**
     * Called before the game starts to setup the game state.
     * @param state
     * @return
     */
    public GlobalState onStartGame(GlobalState state) {
        return state;
    }

    /**
     * Called after a player ended his turn.
     * @param state
     * @return
     */
    public GlobalState onEndTurn(GlobalState state) {
        return state;
    }

    /**
     * Called after a player satisfies a winning condition
     * @param state
     * @param player The Player that has finished
     * @return
     */
    public GlobalState onPlayerFinished(GlobalState state, Player player){
        return state;
    }
}
