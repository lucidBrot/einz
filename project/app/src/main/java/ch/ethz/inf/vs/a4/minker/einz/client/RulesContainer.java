package ch.ethz.inf.vs.a4.minker.einz.client;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.RuleLoader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.*;
import ch.ethz.inf.vs.a4.minker.einz.rules.otherrules.CountNumberOfCardsAsPoints;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RulesContainer {

    private JSONObject cardRules = new JSONObject();
    private JSONArray globalRules = new JSONArray();

    private static RulesContainer defaultInstance = null;

    private EinzMessageHeader header = new EinzMessageHeader("startgame", "SpecifyRules");

    public EinzMessage<EinzSpecifyRulesMessageBody> toMessage() {
        return new EinzMessage<>(header, this.toMessageBody());
    }

    public EinzSpecifyRulesMessageBody toMessageBody() {
        if (this.cardRules.length() <= 0) {
            this.addCard("debug", 1);
        }
        return new EinzSpecifyRulesMessageBody(this.cardRules, this.globalRules);
    }

    public synchronized void addGlobalRule(BasicGlobalRule rule) {
        globalRules.put(ruleToJSON(rule));
    }

    /**
     * Highly inefficient.
     * @return a new instance with the parameters as set here or null if not set at all
     */
    public synchronized BasicGlobalRule getGlobalRule(String ruleName){
        int i = 0;
        JSONObject ruleObj = globalRules.optJSONObject(i);
        String id;
        if(ruleObj!=null)id = ruleObj.optString("id"); else {return null;}

        while (ruleObj != null && !id.equals(ruleName)) {
            if (id.equals(ruleName)) {
                break;
            }
            i++;
            ruleObj = globalRules.optJSONObject(i);
            if(ruleObj!=null) {id = ruleObj.optString("id");} else {break;}
        }
        if(ruleObj!=null){
            // found id at index i
            BasicGlobalRule rule = (BasicGlobalRule) EinzSingleton.getInstance().getRuleLoader().getInstanceOfRule(id);
            JSONObject params = ruleObj.optJSONObject("parameters");
            if(params!=null && rule instanceof ParametrizedRule && rule != null){
                try {
                    ((ParametrizedRule) rule).setParameter(params);
                } catch (JSONException e) {
                    Log.e("RulesContainer", "failed setting rule parameters"+ params.toString());
                    e.printStackTrace();
                }
            }
            return rule;
        }

        return null;
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
     * Overwrites previous settings for this (rule, cardID) combination
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
        if (!someCardID.has("number")) {
            setNumberOfCards(cardID, "1");
        }
    }

    public synchronized int getNumberOfCards(String cardID){
        if (!cardRules.has(cardID)) {
            return 0;
        }
        try {
            return cardRules.getJSONObject(cardID).getInt("number");
        } catch (JSONException e) {
            return 0;
        }
    }

    /**
     * sets the Number of Cards in the deck of type cardID.
     * (Adds the number to the Card. if the internal mapping is inexistent, it creates it. If there is already a number set, it will be overwritten.)
     * fails with a log message and returns if the input number is bad.
     * Do not pass negative numbers
     */
    public synchronized void setNumberOfCards(String cardID, String number) {
        try {
            if (Integer.valueOf(number) < 0) {
                Log.w("RulesContainer", "bad number " + number + " for card " + cardID);
                return;
            }
        } catch (Exception e) {
            Log.w("RulesContainer", "bad number " + number + " for card " + cardID);
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
    public synchronized void addCard(String cardId, Integer number) {
        this.setNumberOfCards(cardId, String.valueOf(number));
    }

    /**
     * Convenience function to do <br><code>
     * <p>
     * addCardRule(cardRule, cardID);<br>
     * setNumberOfCards(cardID, number);
     * </code><br> in one line
     */
    public synchronized void addCardRuleWithNumber(BasicCardRule cardRule, String cardID, String number) {
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
        JSONObject params = new JSONObject();
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

    /**
     * returns a container with the default rules loaded
     */
    public static RulesContainer getDefaultRulesInstance() {
        RulesContainer container = new RulesContainer();
        if (defaultInstance == null) {
            defaultInstance = container;
        } else {
            return defaultInstance; // don't compute multiple times
        }

        // load deck
        CardLoader cardLoader = EinzSingleton.getInstance().getCardLoader();
        for (String cardID : cardLoader.getCardIDs()) {
            if (!cardID.equals("debug")) { // add all cards except debug card
                container.addCard(cardID, 2); // add every card twice into the deck
            }
        }

        // load global rules
        StartGameWithCardsRule myStartGameWithCardsRule = new StartGameWithCardsRule();
        try {
            JSONObject param = new JSONObject();
            param.put(StartGameWithCardsRule.getParameterName(), 7);
            myStartGameWithCardsRule.setParameter(param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        container.addGlobalRule(myStartGameWithCardsRule);
        container.addGlobalRule(new WinOnNoCardsRule());
        container.addGlobalRule(new CountNumberOfCardsAsPoints());
        container.addGlobalRule(new NextTurnRule());
        container.addGlobalRule(new NextTurnRule2()); // one of these is for starting the next turn after drawing, the other for starting after playing

        // load card rules
        //                        arr.add(new PlayColorRule());
//                        arr.add(new PlayTextRule());
//                        arr.add(new IsValidDrawRule());
//                        tempCardRules.put(card.getID(), arr);
        for (String cardID : cardLoader.getCardIDs()) {
            switch (cardID) {
                case "debug": {
                    container.addCardRuleWithNumber(new PlayColorRule(), cardID, "0");
                    container.addCardRuleWithNumber(new PlayTextRule(), cardID, "0");
                    container.addCardRuleWithNumber(new IsValidDrawRule(), cardID, "0");
                    container.addCardRuleWithNumber(new PlayAlwaysRule(), cardID, "0");
                    break;
                }
                case "take4": {
                    container.addCardRule(new PlayColorRule(), cardID);
                    container.addCardRule(new PlayTextRule(), cardID);
                    container.addCardRule(new IsValidDrawRule(), cardID);
                    container.addCardRule(new PlayAlwaysRule(), cardID);
                    container.addCardRule(new WishColorRule(), cardID);
                    break;
                }
                case "choose": {
                    container.addCardRule(new PlayColorRule(), cardID);
                    container.addCardRule(new PlayTextRule(), cardID);
                    container.addCardRule(new IsValidDrawRule(), cardID);
                    container.addCardRule(new PlayAlwaysRule(), cardID);
                    container.addCardRule(new WishColorRule(), cardID);
                    break;
                }
                default: {
                    container.addCardRule(new PlayColorRule(), cardID);
                    container.addCardRule(new PlayTextRule(), cardID);
                    container.addCardRule(new IsValidDrawRule(), cardID);
                    // differentiate further
                    Card tempCard = cardLoader.getCardInstance(cardID);
                    switch (tempCard.getText()) {
                        case STOP: {
                            container.addCardRule(new SkipRule(), cardID);
                            break;
                        }
                        case PLUSTWO: {
                            container.addCardRule(new DrawTwoCardsRule(), cardID);
                            break;
                        }
                        case SWITCHORDER: {
                            container.addCardRule(new ChangeDirectionRule(), cardID);
                        }
                    }
                    break;
                }

            }
        }
        // now we have registered cardRules
        defaultInstance = container;
        return defaultInstance;
    }

    /**
     * calls {@link #addCardRule} only if the card already has a number. This has the effect that the previously set number will always be kept
     * @param cardRule
     * @param cardID
     */
    public void addCardRuleKeepPreviousNumber(BasicCardRule cardRule, String cardID) {
        if(this.cardRules.optJSONObject(cardID).has("number")){
            addCardRule(cardRule, cardID);
        }
    }
}
