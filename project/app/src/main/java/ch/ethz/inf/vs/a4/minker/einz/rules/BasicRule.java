package ch.ethz.inf.vs.a4.minker.einz.rules;

import org.json.JSONObject;

/**
 * Basic class every Rule has to implement.
 */
public abstract class BasicRule {

    /**
     * Access to the game Config. Here the rules can get a new drawPile or get all spectators etc.
     */
    protected GameConfig config;

    public BasicRule() { }

    public void initialize(GameConfig config) {
        this.config = config;
    }

    /**
     * The Name of the Rule. Used as identifier for the rule
     * @return The Name of the Rule
     */
    public abstract String getName();

    /**
     * Describes what the rule is doing.
     * @return Description of the rule.
     */
    public abstract String getDescription();

    public abstract void setParameters(JSONObject parameters);


    public JSONObject getContentAsJSON() {
        return new JSONObject();
        // TODO: implement this if needed
    }
}
