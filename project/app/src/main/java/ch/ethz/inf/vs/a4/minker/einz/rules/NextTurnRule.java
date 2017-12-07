package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import org.json.JSONObject;

/**
 * Created by Fabian on 04.12.2017.
 */

public class NextTurnRule extends BasicGlobalRule {
    @Override
    public String getName() {
        return "Next players turn";
    }

    @Override
    public String getDescription() {
        return "After a player has played a card, his turn is finished and it is the next players turn.";
    }

    @Override
    public void setParameters(JSONObject parameters) {
        // TODO: implement setParameters
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played){
        state.nextTurn();
        return state;
    }
}
