package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzSpecifyRulesMessageBody extends EinzMessageBody {

    private final JSONObject cardRules; // contains a list of JSONObjects, each having id and parameters - for every CardID
    private final JSONArray globalRules; // contains a list of JSONObjects, each having id and parameters

    // card rules list as actual list of JSONObjects. Those contain id and params of the rules

    /**
     * @param cardRules
     * @param globalRules
     */
    public EinzSpecifyRulesMessageBody(JSONObject cardRules, JSONArray globalRules) {
        this.cardRules = cardRules;
        this.globalRules = globalRules;
    }

    public JSONObject getCardRules() {
        return cardRules;
    }

    public JSONArray getGlobalRules() {
        return globalRules;
    }

    /**
     * <img src="../parsertypes/doc-files/etjgJ2D.jpg"/><br>Consider this an easter-egg
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("cardRules",this.cardRules);
        body.put("globalRules", this.globalRules);
        return body;
    } 
}
