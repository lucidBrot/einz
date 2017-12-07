package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.gamelogic.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.Player;
import org.json.JSONObject;

/**
 * Created by Josua on 11/24/17.
 */

public class WinOnNoCardsRule extends BasicGlobalRule {

    @Override
    public String getName() {
        return "Play all cards";
    }

    @Override
    public String getDescription() {
        return "A player is finished if he has played all his cards";
    }

    @Override
    public boolean isPlayerFinished(GlobalState state, Player player) {
        return player.hand.size() == 0;
    }
}
