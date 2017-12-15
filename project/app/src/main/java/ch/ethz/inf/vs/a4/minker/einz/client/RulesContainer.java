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
        if(this.cardRules.length()<=0){
            this.addCard("debug", 1);
        }
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


    /**
     * Adds the card rule to the internal mapping and sets the number of cards of this type to 1 if no other value was set yet.
     */
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
        if(!someCardID.has("number")){
            setNumberOfCards(cardID, "1");
        }
    }

    /**
     * sets the Number of Cards in the deck of type cardID.
     * (Adds the number to the Card. if the internal mapping is inexistent, it creates it. If there is already a number set, it will be overwritten.)
     * fails with a log message and returns if the input number is bad.
     * Do not pass negative numbers
     * */
    public synchronized void setNumberOfCards(String cardID, String number) {
        try {
            if(Integer.valueOf(number) < 0){
                Log.w("RulesContainer", "bad number "+number+" for card "+cardID);
                return;
            }
        } catch (Exception e){
            Log.w("RulesContainer", "bad number "+number+" for card "+cardID);
            e.printStackTrace();
            return;
        }
        try {
            if (!cardRules.has(cardID)) {
                cardRules.put(cardID, new JSONObject());
            }
            cardRules.getJSONObject(cardID).put("number", number);
        } catch (JSONException e) {
            Log.w("RulesContainer", "setting number of cards failed.");
            e.printStackTrace();

        }

        JSONArray rulelist = null;
        try {
            rulelist = cardRules.getJSONObject(cardID).optJSONArray("rulelist");
            if (rulelist == null) {
                rulelist = new JSONArray();
                cardRules.getJSONObject(cardID).put("rulelist", rulelist);

            }
        } catch (JSONException e) {
            Log.w("RulesContainer", "Tried adding a CardRule but failed v2");
            return;
        }


    }

    /**
     * Same as {@link #setNumberOfCards}
     */
    public synchronized void addCard(String cardId, Integer number){
        this.setNumberOfCards(cardId, String.valueOf(number));
    }

    /**
     * Convenience function to do <br><code>
     *
     addCardRule(cardRule, cardID);<br>
     setNumberOfCards(cardID, number);
     * </code><br> in one line
     * */
    public synchronized void addCardRuleWithNumber(BasicCardRule cardRule, String cardID, String number){
        addCardRule(cardRule, cardID);
        setNumberOfCards(cardID, number);
    }

    /**
     * Removes the card with all its mappings if it exists, does nothing otherwise
     *
     * @return false if not found, true if removed
     */
    public synchronized void removeCard(String cardID) {
        cardRules.remove(cardID);
    }

    /**
     * Removes the cardRule specific to the card specified. Does nothing if not there
     */
    public synchronized void removeCardRuleFromCard(String ruleName, String cardID) {
        try {
            cardRules.getJSONObject(cardID).remove(ruleName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param ruleName will be removed from any card mappings it was in
     */
    public synchronized void removeCardRule(String ruleName) {
        while (cardRules.keys().hasNext()) {
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
