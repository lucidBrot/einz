package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;

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

    @Override
    public GlobalState onPlayAssignedCard(GlobalState state, Card played) {
        List<Player> players = state.getPlayersOrdered();
        int currentPlayer = players.indexOf(state.getActivePlayer());
        state.playOrderIsForwards = !state.playOrderIsForwards;
        int nextPlayerIndex = (currentPlayer + (state.playOrderIsForwards? 1 : -1) + players.size()) % players.size();
        state.nextPlayer = players.get(nextPlayerIndex);
        return state;
    }
}
