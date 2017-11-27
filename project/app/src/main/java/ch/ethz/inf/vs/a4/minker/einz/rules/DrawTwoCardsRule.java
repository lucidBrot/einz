package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;

/**
 * Created by Josua on 11/24/17.
 */

public class DrawTwoCardsRule extends BasicCardRule{

    private boolean assignedCardPlayed = false;

    public DrawTwoCardsRule(GameConfig config, Card assignedTo) {
        super(config, assignedTo);
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
        return state.getTopCardDiscardPile().text.equals(played.text) || !assignedCardPlayed;
    }

    @Override
    public GlobalState onDrawCard(GlobalState state) {
        if(assignedCardPlayed) {
            state.setPermissive();
            state.cardsToDraw = 1;
            assignedCardPlayed = false;
        }
        return state;
    }

    @Override
    public GlobalState onPlayCard(GlobalState state, Card played) {
        if(played.text.equals(assignedTo.text)){
            if(state.cardsToDraw == 1){
                state.cardsToDraw = 2;
            } else {
                state.cardsToDraw += 2;
            }
            assignedCardPlayed = true;
            state.setRestrictive();
        }
        return state;
    }
}
