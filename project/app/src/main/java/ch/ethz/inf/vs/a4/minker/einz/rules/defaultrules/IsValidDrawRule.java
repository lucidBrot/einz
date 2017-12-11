package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Fabian on 11.12.2017.
 * @Josua: If you have different rules in mind for the rules.defaultrules, feel free to change this
 */

public class IsValidDrawRule extends BasicGlobalRule {

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
         */
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played){
        state.nextTurn();
        return state;
    }
}
