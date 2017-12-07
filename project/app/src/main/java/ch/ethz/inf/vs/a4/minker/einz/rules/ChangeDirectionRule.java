package ch.ethz.inf.vs.a4.minker.einz.rules;

import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import org.json.JSONObject;

/**
 * Created by Josua on 11/24/17.
 */

public class ChangeDirectionRule extends BasicCardRule {

    @Override
    public String getName() {
        return "Reverse play Direction";
    }

    @Override
    public String getDescription() {
        return "Reverses the order of the players.";
    }

    /**
     * Not yet implemented!
     * @param parameters
     */
    @Override
    public void setParameters(JSONObject parameters) {

    }

    @Override
    public GlobalState onPlayAssignedCard(GlobalState state, Card played) {
        List<Player> players = state.getPlayersOrdered();
        int currentPlayer = players.indexOf(state.getActivePlayer());
        int nextPlayerIndex = (currentPlayer + (state.playOrderIsForwards? 1 : -1 + players.size() )) % players.size();
        state.nextPlayer = players.get(nextPlayerIndex);
        state.playOrderIsForwards = !state.playOrderIsForwards;
        return state;
    }
}
