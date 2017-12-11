package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Fabian on 11.12.2017.
 * @Josua: If you have different rules in mind for the rules.defaultrules, feel free to change this
 */

public class SkipRule extends BasicCardRule {

    @Override
    public String getName() {
        return "Skip turn";
    }

    @Override
    public String getDescription() {
        return "when the Card is played, the next Players turn is skipped.";
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played) {
        if(played.getText().equals(assignedTo.getText()) && played.getColor().equals(assignedTo.getColor())){
            state.nextTurn(); //Only call this once, because it gets called another time in NextTurnRule
        }
        return state;
    }
}
