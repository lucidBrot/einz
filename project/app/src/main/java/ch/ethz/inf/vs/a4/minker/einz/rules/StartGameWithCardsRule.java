package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Josua on 11/24/17.
 */

public class StartGameWithCardsRule extends BasicGlobalRule {

    public StartGameWithCardsRule(GameConfig config) {
        super(config);
    }

    @Override
    public String getName() {
        return "Start Cards";
    }

    @Override
    public String getDescription() {
        return "Sets the number of cards the game starts with";
    }

    @Override
    public GlobalState onStartGame(GlobalState state) {
        for(Player player : state.players){
            Card drawnCard = state.drawCard();
            if (drawnCard == null){
                state.addCardsToDrawPile(config.getShuffledDrawPile());
                drawnCard = state.drawCard();
            }
            player.hand.add(drawnCard);
        }
        return state;
    }
}
