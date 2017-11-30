package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzInitGameMessageBody extends EinzMessageBody{

    private final ArrayList<BasicRule> ruleset;
    private final ArrayList<String> turnOrder;

    public EinzInitGameMessageBody(ArrayList<BasicRule> ruleset, ArrayList<String> turnOrder) {
        this.ruleset = ruleset;
        this.turnOrder = turnOrder;
    }

    public ArrayList<BasicRule> getRuleset() {
        return ruleset;
    }

    public ArrayList<String> getTurnOrder() {
        return turnOrder;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        JSONObject ruleset = new JSONObject();
        for(BasicRule rule : this.ruleset){
            ruleset.put(rule.getName(), rule.getContentAsJSON());
        }
        body.put("ruleset",ruleset);
        JSONArray turnOrder = new JSONArray(this.turnOrder);
        body.put("turn-order", turnOrder);
        return body;
    }
}
