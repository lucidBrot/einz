package ch.ethz.inf.vs.a4.minker.einz;


/**
 * Basic rule class for rules not applicable to cards.
 *
 * Example:
 *      Determine number of cards in beginning of a game.
 *      Determine how often a card is in the deck
 */
public abstract class BasicGlobalRule extends BasicRule {

    public BasicGlobalRule(GameConfig config) {
        super(config);
    }

    public boolean isValidEndTurn(GlobalState state) {
        return false;
    }

    public boolean isPlayerFinished(GlobalState state, Player player) {
        return false;
    }


    public GlobalState onStartGame(GlobalState state) {
        return state;
    }

    public GlobalState onEndTurn(GlobalState state) {
        return state;
    }

    public GlobalState onPlayerFinished(GlobalState state, Player player){
        return state;
    }
}
