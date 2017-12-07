package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzInitGameMessageBody extends EinzMessageBody{

    private final JSONObject cardRules;
    private final JSONArray globalRules;
    private final ArrayList<String> turnOrder;

    /**
     * <img src="../../../../../../../../../../../../../../protocols/docScreenshots/InitGameJSON.png"/>
     * @param cardRules
     * @param globalRules
     * @param turnOrder
     */
    public EinzInitGameMessageBody(JSONObject cardRules, JSONArray globalRules, ArrayList<String> turnOrder) {
        this.turnOrder = turnOrder;
        this.cardRules = cardRules;
        this.globalRules = globalRules;
    }

    public JSONObject getCardRules() {
        return cardRules;
    }

    public JSONArray getGlobalRules() {
        return globalRules;
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
        body.put("cardRules",this.cardRules);
        body.put("globalRules", this.globalRules);
        JSONArray turnOrder = new JSONArray(this.turnOrder);
        body.put("turn-order", turnOrder);
        return body;
    }
}
