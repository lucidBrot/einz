package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.*;
import ch.ethz.inf.vs.a4.minker.einz.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.cards.CardColor;
import org.json.JSONObject;

/**
 * Created by Josua on 11/27/17.
 */

public class WishColorRule extends BasicCardRule {
    /**
     * Not yet implemented!
     * @param parameters
     */
    @Override
    public void setParameters(JSONObject parameters) {

    }

    private CardColor wishedColor = null;

    private boolean wished = false; // #bamboozled TODO

    @Override
    public String getName() {
        return "Wish color";
    }

    @Override
    public String getDescription() {
        return "Forces the next card to be of the wished color";
    }

    @Override
    public boolean isValidPlayCardPermissive(GlobalState state, Card played) {
        return played.getColor().equals(wishedColor);
    }

    @Override
    public GlobalState onPlayAssignedCard(GlobalState state, Card played) {
        wished = true;
        return state;
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played) {
        if(!assignedTo.equals(played)){
            wished = false;
        }
        return state;
    }
}
