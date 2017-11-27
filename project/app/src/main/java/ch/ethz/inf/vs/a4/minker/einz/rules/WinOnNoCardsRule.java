package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;

/**
 * Created by Josua on 11/24/17.
 */

public class WinOnNoCardsRule extends BasicGlobalRule {

    public WinOnNoCardsRule(GameConfig config) {
        super(config);
    }

    @Override
    public String getName() {
        return "Play all cards";
    }

    @Override
    public String getDescription() {
        return "A player is finished if he has played all his cards";
    }

}
