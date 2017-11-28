package ch.ethz.inf.vs.a4.minker.einz.rules;

import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Josua on 11/24/17.
 */

public class StartGameWithCardsRule extends BasicGlobalRule {

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
                startHand = state.drawCards(7);
            }
            player.hand.addAll(startHand);
        }
        return state;
    }
}
