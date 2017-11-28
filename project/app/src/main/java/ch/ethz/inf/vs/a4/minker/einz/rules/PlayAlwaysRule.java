package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;

/**
 * Created by Josua on 11/24/17.
 */

public class PlayAlwaysRule extends BasicCardRule{

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
