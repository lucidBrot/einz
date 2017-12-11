package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Fabian on 11.12.2017.
 * @Josua: If you have different rules in mind for the rules.defaultrules, feel free to change this
 */

public class NextTurnRule2 extends BasicGlobalRule {
    @Override
    public String getName() {
        return "Can't play after draw";
    }

    @Override
    public String getDescription() {
        return "After a player draws cards, his turn is finished and it is the next players turn.";
    }

    @Override
    public GlobalState onDrawCard(GlobalState state){
        state.nextTurn();
        return state;
    }
}
