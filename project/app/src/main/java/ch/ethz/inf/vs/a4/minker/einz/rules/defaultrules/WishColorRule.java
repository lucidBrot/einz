package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by Josua on 11/27/17.
 */

public class WishColorRule extends BasicCardRule {

    private CardColor wishedColor = null;

    private boolean wished = false;

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
        ArrayList<String> options = new ArrayList<>();
        for(CardColor color : CardColor.values()){
            options.add(color.color);
        }

        String result = config.getClientCallbackService().getSelectionFromPlayer(state.getActivePlayer(), options);
        wishedColor = CardColor.valueOf(result);
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