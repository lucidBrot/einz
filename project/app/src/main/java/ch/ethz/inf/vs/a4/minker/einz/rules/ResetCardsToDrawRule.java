package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.cards.CardText;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import org.json.JSONObject;

/**
 * Created by Fabian on 01.12.2017.
 */

public class ResetCardsToDrawRule extends BasicGlobalRule {
    /**
     * Not yet implemented!
     * @param parameters
     */
    @Override
    public void setParameters(JSONObject parameters) {

    }

    @Override
    public String getName() {
        return "Resets the cardsToDraw field";
    }

    @Override
    public String getDescription() {
        return "After a player has drawn cards, the cardsToDraw get reset to 1, since the effects of "+
                CardText.PLUSTWO.type + " and " + CardText.CHANGECOLORPLUSFOUR.type + "end.";
    }

    @Override
    public GlobalState onDrawCard(GlobalState state){
        state.setCardsToDraw(1);
        return state;
    }

}
