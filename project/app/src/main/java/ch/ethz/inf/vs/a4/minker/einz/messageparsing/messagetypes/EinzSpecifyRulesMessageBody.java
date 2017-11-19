package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.Rule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzSpecifyRulesMessageBody extends EinzMessageBody {

    private final ArrayList<Rule> ruleset;

    /**
     * @param ruleset an ArrayList of Rules to set
     */
    public EinzSpecifyRulesMessageBody(ArrayList<Rule> ruleset) {
        this.ruleset = ruleset;
    }

    public ArrayList<Rule> getRuleset() {
        return ruleset;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        JSONObject ruleset = new JSONObject();
        for(Rule rule : this.ruleset){
            ruleset.put(rule.getIdentifier(), rule.getContentAsJSON());
        }
        body.put("ruleset",ruleset);
        return body;
    }
}
