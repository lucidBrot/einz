package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import org.json.JSONObject;

/**
 * Created by Josua on 11/24/17.
 */

public class DrawTwoCardsRule extends BasicCardRule{

    private boolean assignedCardPlayed = false;
    /**
     * Not yet implemented!
     * @param parameters
     */
    @Override
    public void setParameters(JSONObject parameters) {

    }

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
        if(played.getText().equals(assignedTo.getText())){
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
