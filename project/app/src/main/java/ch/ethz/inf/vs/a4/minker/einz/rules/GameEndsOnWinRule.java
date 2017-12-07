package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.gamelogic.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.Player;
import org.json.JSONObject;

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
