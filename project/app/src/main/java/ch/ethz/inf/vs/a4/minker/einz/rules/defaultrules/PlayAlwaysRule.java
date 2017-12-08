package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Josua on 11/24/17.
 */

public class PlayAlwaysRule extends BasicCardRule {

    @Override
    public String getName() {
        return "Play always";
    }

    @Override
    public String getDescription() {
        return "Allows to play the card no matter what card lies on the stack";
    }

    @Override
    public boolean isValidPlayCardPermissive(GlobalState state, Card played) {
        return true;
    }
}
