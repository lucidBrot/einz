package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Fabian on 11.12.2017.
 * TODO: Change this to BasicGlobalRule
 */

public class IsValidDrawRule extends BasicCardRule {

    @Override
    public String getName() {
        return "Player can draw";
    }

    @Override
    public String getDescription() {
        return "A player can always draw cards.";
        /*
         * This is only useful if it gets checked somewhere else if it is that players turn
         * and a player can not play anything after he draws cards. (e.g. NextTurnRule2 is active).
         * Currently This CardRule gets assigned to the YELLOW_0 card just so that it works
         * This is not clean and should be changed when someone has time
         */
    }

    @Override
    public boolean isValidDrawCardsPermissive(GlobalState state){
        return true;
    }
}
