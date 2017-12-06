package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.*;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class EinzSpecifyRulesMessageBody extends EinzMessageBody {

    private final JSONObject cardRules; // contains a list of JSONObjects, each having id and parameters - for every CardID
    private final JSONArray globalRules; // contains a list of JSONObjects, each having id and parameters
    private HashMap<Card, ArrayList<BasicCardRule>> parsedCardRules;
    private HashMap<Card, Integer> cardNumbers;

    // card rules list as actual list of JSONObjects. Those contain id and params of the rules


    /**
     * <img src="../../../../../../../../../../../../../../protocols/docScreenshots/SpecifyRulesJSON.png"/>
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


    /**
     * @return false if failed, else true
     * Parses what it needs to later access that
     */
    private boolean parseFurther(){
        HashMap<Card, ArrayList<BasicCardRule>> parsedCardRules = new HashMap<>();
        HashMap<Card, Integer> cardNumbers = new HashMap<>();
        CardLoader cl = new CardLoader();
        Iterator<String> cards = this.cardRules.keys();
        RuleLoader rl = new RuleLoader();
        try {
            while (cards.hasNext()) {
                ArrayList<BasicCardRule> rules = new ArrayList<>();
                String cardIDKey = cards.next();
                JSONObject object = this.cardRules.getJSONObject(cardIDKey);
                int numberOfCopies = object.getInt("number");
                Card card = cl.getCardInstance(cardIDKey);
                cardNumbers.put(card, numberOfCopies);

                JSONArray arr = object.getJSONArray("rulelist");
                for (int i=0; i<object.getJSONArray("rulelist").length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    String id = o.getString("id");
                    JSONObject parameters = o.getJSONObject("parameters");
                    BasicCardRule rule = (BasicCardRule) rl.getInstanceOfRule(id);
                    rule.setParameters(parameters);
                    rules.add(rule);

                }
                parsedCardRules.put(card, rules);

            }
            this.parsedCardRules = parsedCardRules;
            this.cardNumbers = cardNumbers;
        }catch (Exception e){
            return false;
        }
        return true;
    }
}
