package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Fabian on 01.12.2017.
 */

public class ResetCardsToDrawRule extends BasicGlobalRule {

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
