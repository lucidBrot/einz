package ch.ethz.inf.vs.a4.minker.einz.rules.otherrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;

public class CountCardValuesAsPoints extends BasicGlobalRule {
    /**
     * The Name of the Rule. Used as identifier for the rule
     *
     * @return The Name of the Rule
     */
    @Override
    public String getName() {
        return "CountCardValuesAsPoints";
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
        return super.onPlayerFinished(state, player);
    }

    /**
     * Describes what the rule is doing.
     *
     * @return Description of the rule.
     */
    @Override
    public String getDescription() {
        return "The ranking is determined by the cards value you still have.";
    }
}
