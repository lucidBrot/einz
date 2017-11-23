package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzSpecifyRulesMessageBody extends EinzMessageBody {

    private final ArrayList<BasicRule> ruleset;

    /**
     * @param ruleset an ArrayList of Rules to set
     */
    public EinzSpecifyRulesMessageBody(ArrayList<BasicRule> ruleset) {
        this.ruleset = ruleset;
    }

    public ArrayList<BasicRule> getRuleset() {
        return ruleset;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        JSONObject ruleset = new JSONObject();
        for(BasicRule rule : this.ruleset){
            ruleset.put(rule.getIdentifier(), rule.getContentAsJSON());
        }
        body.put("ruleset",ruleset);
        return body;
    }
}
