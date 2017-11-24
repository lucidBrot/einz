package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;

/**
 * Created by Josua on 11/22/17.
 */

public class PlayColorRule extends BasicRule{

    @Override
    public String getName() {
        return "Play Color on Color";
    }

    @Override
    public String getDescription() {
        return "Enables to play the card on top of a card with the same color";
    }

    @Override
    public boolean isValidPlayCard(GlobalState state, Card played){
        return state.getDiscardPile().get(state.getDiscardPile().size() - 1).color == played.color;
    }
}
