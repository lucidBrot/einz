package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Josua on 11/24/17.
 */

public class PlayTextRule extends BasicCardRule {

    @Override
    public String getName() {
        return "Play on equal text";
    }

    @Override
    public String getDescription() {
        return "Enables to play a card on top of one with the same text";
    }

    @Override
    public boolean isValidPlayCardPermissive(GlobalState state, Card played) {
        return state.getTopCardDiscardPile().getText().equals(played.getText());
    }
}
