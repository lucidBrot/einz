package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardColors;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;

/**
 * Created by Josua on 11/27/17.
 */

public class WishColorRule extends BasicCardRule {

    private CardColors wishedColor = null;

    public WishColorRule(GameConfig config, Card assignedTo) {
        super(config, assignedTo);
    }

    @Override
    public String getName() {
        return "Wish color";
    }

    @Override
    public String getDescription() {
        return "Forces the next card to be of the wished color";
    }

    @Override
    public boolean isValidPlayCardRestrictive(GlobalState state, Card played) {
        return played.color.equals(wishedColor);
    }

    @Override
    public GlobalState onPlayCard(GlobalState state, Card played) {
        state.setRestrictive();
        config
        return state;
    }
}
