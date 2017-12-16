package ch.ethz.inf.vs.a4.minker.einz.rules.otherrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Less points is better
 */
public class CountNumberOfCardsAsPoints extends BasicGlobalRule {
    /**
     * The Name of the Rule. Used as identifier for the rule
     *
     * @return The Name of the Rule
     */
    @Override
    public String getName() {
        return "CountNumberOfCardsAsPoints";
    }

    /**
     * Called after a player satisfies a winning condition
     *
     * @param state
     * @return
     */
    @Override
    public GlobalState onGameOver(GlobalState state) {
        for(Player p : state.getAllPlayers()){
            state.setPointsForPlayer(p.getName(), p.hand.size());
        }
        return state;
    }

    /**
     * Called after a player satisfies a winning condition.
     * Ends the game.
     *
     * @param state
     * @param player The Player that has finished
     * @return
     */
    @Override
    public GlobalState onPlayerFinished(GlobalState state, Player player) {
        state.finishGame();
        return state;
    }

    /**
     * Describes what the rule is doing.
     *
     * @return Description of the rule.
     */
    @Override
    public String getDescription() {
        return "The ranking is determined by the number of cards value you still have.";
    }
}
