package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;

/**
 * Created by Josua on 11/24/17.
 */

public class ChangeDirectionRule extends BasicCardRule {

    public ChangeDirectionRule(GameConfig config, Card assignedTo) {
        super(config, assignedTo);
    }

    @Override
    public String getName() {
        return "Change play Direction";
    }

    @Override
    public String getDescription() {
        return "Changes the order of the players.";
    }
}
