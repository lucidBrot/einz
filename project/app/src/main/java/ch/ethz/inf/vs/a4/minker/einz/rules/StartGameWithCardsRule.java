package ch.ethz.inf.vs.a4.minker.einz.rules;

import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.ParametizedRule;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import org.json.JSONObject;

/**
 * Created by Josua on 11/24/17.
 */

public class StartGameWithCardsRule extends BasicGlobalRule implements ParametizedRule{
    /**
     * Not yet implemented!
     * @param parameters
     */
    @Override
    public void setParameters(JSONObject parameters) {

    }

    private int startCards = 7;

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
            List<Card> startHand = state.drawCards(7);
            if (startHand == null){
                state.addCardsToDrawPile(config.getShuffledDrawPile());
                startHand = state.drawCards(startCards);
            }
            player.hand.addAll(startHand);
        }
        return state;
    }

    @Override
    public void setParameter() {

    }
}
