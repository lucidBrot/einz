package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Fabian on 04.12.2017.
 */

public class NextTurnRule extends BasicGlobalRule {

    // This rule won't work because the user can arrange them in arbitrary order so next turn could
    // be before some special abilities were executed. This problem has to be resolved another way

    @Override
    public String getName() {
        return "Next players turn";
    }

    @Override
    public String getDescription() {
        return "After a player has played a card, his turn is finished and it is the next players turn.";
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played){
        state.nextTurn();
        return state;
    }
}
