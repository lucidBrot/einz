package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import org.json.JSONObject;

/**
 * Created by Josua on 11/24/17.
 */

public class PlayAlwaysRule extends BasicCardRule{
    /**
     * Not yet implemented!
     * @param parameters
     */
    @Override
    public void setParameters(JSONObject parameters) {

    }

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
