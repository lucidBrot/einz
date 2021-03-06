package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Josua on 11/22/17.
 */

public class PlayColorRule extends BasicCardRule {

    @Override
    public String getName() {
        return "Play Color on Color";
    }

    @Override
    public String getDescription() {
        return "Enables to play the card on top of a card with the same color";
    }

    @Override
    public boolean isValidPlayCardPermissive(GlobalState state, Card played){
        return state.getTopCardDiscardPile().getColor() == played.getColor();
    }
}
