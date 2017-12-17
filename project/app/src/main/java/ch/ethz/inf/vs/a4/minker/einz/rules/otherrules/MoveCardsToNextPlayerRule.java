package ch.ethz.inf.vs.a4.minker.einz.rules.otherrules;

import org.json.JSONException;

import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Josua on 12/17/17.
 */

public class MoveCardsToNextPlayerRule extends BasicCardRule {
    @Override
    public String getName() {
        return "Move Hand to Next Player";
    }

    @Override
    public String getDescription() {
        return "Moves the hand of every player to the next player";
    }

    @Override
    public GlobalState onPlayAssignedCard(GlobalState state, Card played) {
        List<Player> players = state.getPlayersOrdered();
        List<Card> firstHand = players.get(0).hand;
        int i = 0;
        for( ; i+1 < players.size(); i++){
            players.get(i).hand = players.get(i+1).hand;
        }
        players.get(i).hand = firstHand;
        return state;
    }


}
