package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.CardText;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;

/**
 * Created by Fabian on 01.12.2017.
 */

public class ResetCardsToDrawRule extends BasicCardRule {

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
