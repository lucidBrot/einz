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
     * @param player The Player that has finished
     * @return
     */
    @Override
    public GlobalState onPlayerFinished(GlobalState state, Player player) {
        state.finishGame();
        for(Player p : state.getPlayersOrdered()){
            int i = 0;
            for(Card c : p.hand){
                i++;
            }
            state.getPoints().put(p.getName(), i);
        }
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
