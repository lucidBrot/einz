package ch.ethz.inf.vs.a4.minker.einz.rules;

import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Josua on 11/24/17.
 */

public class ChangeDirectionRule extends BasicCardRule {

    public ChangeDirectionRule(GameConfig config, Card assignedTo) {
        super(config, assignedTo);
    }

    @Override
    public String getName() {
        return "Reverse play Direction";
    }

    @Override
    public String getDescription() {
        return "Reverses the order of the players.";
    }

    @Override
    public GlobalState onPlayCard(GlobalState state, Card played) {
        List<Player> players = state.getPlayersOrdered();
        int currentPlayer = players.indexOf(state.getActivePlayer());
        int nextPlayerIndex = (currentPlayer + (state.playOrderIsForwards? 1 : -1 + players.size() )) % players.size();
        state.nextPlayer = players.get(nextPlayerIndex);
        state.playOrderIsForwards = !state.playOrderIsForwards;
        return state;
    }
}
