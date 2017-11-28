package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Josua on 11/27/17.
 */

public class GameEndsOnWinRule extends BasicGlobalRule {
    public GameEndsOnWinRule(GameConfig config) {
        super(config);
    }

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
