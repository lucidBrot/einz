package ch.ethz.inf.vs.a4.minker.einz.client;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RulesContainer {

    private JSONObject cardRules = new JSONObject();
    private JSONArray globalRules = new JSONArray();

    private EinzMessageHeader header = new EinzMessageHeader("startgame", "SpecifyRules");

    public EinzMessage<EinzSpecifyRulesMessageBody> toMessage() {
        return new EinzMessage<>(header, this.toMessageBody());
    }

    public EinzSpecifyRulesMessageBody toMessageBody() {
        return new EinzSpecifyRulesMessageBody(this.cardRules, this.globalRules);
    }

    public synchronized void addGlobalRule(BasicGlobalRule rule) {
        globalRules.put(ruleToJSON(rule));
    }

    /**
     * Removes the first rule with equivalent content to <code>rule</code>
     *
     * @return false if not found, true if removed
     */
    public synchronized boolean removeGlobalRule(BasicGlobalRule rule) {
        for (int i = 0; i < globalRules.length(); i++) {
            try {
                if (globalRules.getJSONObject(i).equals(ruleToJSON(rule))) {
                    globalRules.remove(i);
                    return true;
                }
            } catch (JSONException e) {
                return false;
            }
        }
        return false;
    }


    public synchronized void addCardRule(BasicCardRule rule, String cardID) {
        JSONObject someCardID = this.cardRules.optJSONObject(cardID);
        if (someCardID == null) {
            someCardID = new JSONObject();
            try {
                this.cardRules.put(cardID, someCardID);
            } catch (JSONException e) {
                Log.w("RulesContainer", "Tried adding a CardRule but failed");
                return;
            }
        }

        JSONArray rulelist = someCardID.optJSONArray("rulelist");
        if (rulelist == null) {
            rulelist = new JSONArray();
            try {
                someCardID.put("rulelist", rulelist);
            } catch (JSONException e) {
                Log.w("RulesContainer", "Tried adding a CardRule but failed v2");
                return;
            }
        }

        rulelist.put(ruleToJSON(rule));
    }

    public synchronized void setNumberOfCards(String cardID){

    }

    /**
     * Removes the card with all its mappings if it exists, does nothing otherwise
     *
     * @return false if not found, true if removed
     */
    public synchronized void removeCard(String cardID){
        cardRules.remove(cardID);
    }

    /**
     * Removes the cardRule specific to the card specified. Does nothing if not there
     */
    public synchronized void removeCardRuleFromCard(String ruleName, String cardID){
        try {
            cardRules.getJSONObject(cardID).remove(ruleName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param ruleName will be removed from any card mappings it was in
     */
    public synchronized void removeCardRule(String ruleName){
        while(cardRules.keys().hasNext()){
            String key = cardRules.keys().next();
            JSONObject cardMapping = null;
            try {
                cardMapping = cardRules.getJSONObject(key);
            } catch (JSONException e) {
                Log.e("RulesContainer", "Malformed JSON detected. I'm broken.");
                e.printStackTrace();
            }
            cardMapping.remove(ruleName);
        }
    }

    private JSONObject ruleToJSON(BasicGlobalRule rule) {
        if (rule == null) {
            return new JSONObject();
        }
        String id = rule.getName();
        JSONObject params = null;
        if (rule instanceof ParametrizedRule) {
            params = ((ParametrizedRule) rule).getParameter();
        }
        JSONObject ruleObj = new JSONObject();
        try {
            ruleObj.put("id", id);
            ruleObj.put("parameters", params);
        } catch (JSONException e) {
            Log.e("RulesContainer", "Failed to transform BasicGlobalRule to JSON");
        }
        return ruleObj;
    }

    private JSONObject ruleToJSON(BasicCardRule rule) {
        if (rule == null) {
            return new JSONObject();
        }
        String id = rule.getName();
        JSONObject params = null;
        if (rule instanceof ParametrizedRule) {
            params = ((ParametrizedRule) rule).getParameter();
        }
        JSONObject ruleObj = new JSONObject();
        try {
            ruleObj.put("id", id);
            ruleObj.put("parameters", params);
        } catch (JSONException e) {
            Log.e("RulesContainer", "Failed to transform BasicGlobalRule to JSON");
        }
        return ruleObj;
    }
}
