package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;

/**
 * Created by Josua on 11/27/17.
 */

public class GameEndsOnWinRule extends BasicGlobalRule {

    @Override
    public String getName() {
        return "End game when a Player finishes";
    }

    @Override
    public String getDescription() {
        return "The game ends if one player has satisfied a winning condition";
    }

    @Override
    public GlobalState onPlayerFinished(GlobalState state, Player player) {
        state.finishGame();
        return state;
    }
}
