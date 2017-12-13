package ch.ethz.inf.vs.a4.minker.einz.rules.otherrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * More points is better
 */
public class CountRankFinishedAsPoints extends BasicGlobalRule {
    /**
     * The Name of the Rule. Used as identifier for the rule
     *
     * @return The Name of the Rule
     */
    @Override
    public String getName() {
        return "CountRankFinishedAsPoints";
    }

    /**
     * Describes what the rule is doing.
     *
     * @return Description of the rule.
     */
    @Override
    public String getDescription() {
        return "The sooner you finish compared to other players, the better you are.";
    }

    @Override
    public GlobalState onGameOver(GlobalState state) {
        for (int i = 0; i < state.getFinishedPlayers().size(); i++) {
            state.addPoints(state.getFinishedPlayers().get(i).getName(), state.getFinishedPlayers().size()-i);
            ///<old>/// points.put(state.getFinishedPlayers().get(i).getName(), Integer.toString(state.getFinishedPlayers().size() - i));
        }
        return state;
    }
}
