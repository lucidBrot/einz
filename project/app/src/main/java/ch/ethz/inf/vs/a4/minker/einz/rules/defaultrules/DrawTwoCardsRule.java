package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Josua on 11/24/17.
 */

public class DrawTwoCardsRule extends BasicCardRule {

    private boolean assignedCardPlayed = false;

    @Override
    public String getName() {
        return "Draw Two Cards";
    }

    @Override
    public String getDescription() {
        return "when the Card is played, the next Player has to draw two cards, except if he ha s " +
                "a card with the same text";
    }

    @Override
    public boolean isValidPlayCardRestrictive(GlobalState state, Card played) {
        return state.getTopCardDiscardPile().getText().equals(played.getText()) || !assignedCardPlayed;
    }

    @Override
    public GlobalState onDrawCard(GlobalState state) {
        if(assignedCardPlayed) {
            state.setCardsToDraw(1);
            assignedCardPlayed = false;
        }
        return state;
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played) {
        if(played.getText().equals(assignedTo.getText()) && played.getColor().equals(assignedTo.getColor())){
            if(state.getCardsToDraw() == 1){
                state.setCardsToDraw(2);
            } else {
                state.setCardsToDraw(state.getCardsToDraw() + 2);
            }
            assignedCardPlayed = true;
        }
        return state;
    }
}
