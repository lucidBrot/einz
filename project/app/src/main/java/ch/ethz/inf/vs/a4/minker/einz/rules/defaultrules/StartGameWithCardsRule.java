package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.model.ParameterType;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;

/**
 * Created by Josua on 11/24/17.
 */

public class StartGameWithCardsRule extends BasicGlobalRule implements ParametrizedRule {

    private int startCards = 0;

    private static final String parameterName = "Number Of Cards";

    public String getName() {
        return "Start Cards";
    }

    @Override
    public String getDescription() {
        return "Sets the number of cards the game starts with";
    }

    @Override
    public GlobalState onStartGame(GlobalState state) {
        for(Player player : state.getPlayersOrdered()){
            List<Card> startHand = state.drawCards(startCards);
            if (startHand == null){
                state.addCardsToDrawPile(config.getShuffledDrawPile());
                // If there are not enough cards in the entire game just draw the full pile
                startHand = state.drawCards(Math.min(startCards, state.getDiscardPile().size()));
            }
            player.hand.addAll(startHand);
        }
        return state;
    }

    @Override
    public void setParameter(JSONObject parameter) throws JSONException{
        startCards = parameter.getInt(parameterName);
    }

    @Override
    public Map<String, ParameterType> getParameterTypes() {
        Map<String, ParameterType> types = new HashMap<>();
        types.put(parameterName,ParameterType.NUMBER);
        return types;
    }

    @Override
    public JSONObject getParameter() {
        JSONObject ret = new JSONObject();
        try {
            ret.put(parameterName, startCards);
        } catch (JSONException e) {
            // this is fine
        }
        return ret;
    }

    public static String getParameterName() {
        return parameterName;
    }
}
