package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.model.ParameterType;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;

/**
 * Created by Fabian on 14.12.2017.
 *
 * This is the parametrized version of the DrawTwoCardsRule
 * A player has to draw cards if he doesn't have another card of the assigned cardType to play
 * (So no +2 card is playable on a +4 card or vice versa)
 */

public class DrawCardsRule extends BasicCardRule implements ParametrizedRule {

    private boolean assignedCardPlayed = false;

    private int cardsToDraw = 0;

    private static final String parameterName = "Cards to draw";

    public String getName() {
        return "Draw Cards";
    }

    @Override
    public String getDescription() {
        return "Sets the number of cards to draw to the given amount (if it was 1) and increases" +
                "it by that amount otherwise";
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
                state.setCardsToDraw(cardsToDraw);
            } else {
                state.setCardsToDraw(state.getCardsToDraw() + cardsToDraw);
            }
            assignedCardPlayed = true;
        }
        return state;
    }

    @Override
    public void setParameter(JSONObject parameter) throws JSONException {
        cardsToDraw = parameter.getInt(parameterName);
    }

    @Override
    public Map<String, ParameterType> getParameterTypes() {
        Map<String, ParameterType> types = new HashMap<>();
        types.put(parameterName, ParameterType.NUMBER);
        return types;
    }
}
