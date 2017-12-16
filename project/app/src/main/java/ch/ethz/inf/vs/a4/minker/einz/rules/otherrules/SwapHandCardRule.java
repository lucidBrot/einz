package ch.ethz.inf.vs.a4.minker.einz.rules.otherrules;

import org.json.JSONObject;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.SelectorRule;

/**
 * Created by Josua on 12/16/17.
 */

public class SwapHandCardRule extends BasicCardRule implements SelectorRule {

    @Override
    public String getName() {
        return "Swap Cards";
    }

    @Override
    public String getDescription() {
        return "When the assigned card is played you can choose a different Player and swap your hand" +
                "cards with that player.";
    }

    @Override
    public ArrayList<String> getChoices(GlobalState state) {
        ArrayList<String> result = new ArrayList<>();
        result.addAll(state.getPlayerHandSizeOrdered().keySet());
        return result;
    }

    @Override
    public String getSelectionTitle() {
        return "Select a player to swap your hand with";
    }

    @Override
    public GlobalState onPlayAssignedCardChoice(GlobalState state, JSONObject rulePlayParams) {
        return state;
    }


}
