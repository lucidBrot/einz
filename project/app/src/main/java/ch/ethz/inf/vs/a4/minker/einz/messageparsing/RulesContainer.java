package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import org.json.JSONArray;
import org.json.JSONObject;

public class RulesContainer {

    private JSONObject cardRules = new JSONObject();
    private JSONArray globalRules = new JSONArray();

    private EinzMessageHeader header = new EinzMessageHeader("startgame", "SpecifyRules");

    public EinzMessage<EinzSpecifyRulesMessageBody> toMessage(){
        return new EinzMessage<>(header, this.toMessageBody());
    }

    public EinzSpecifyRulesMessageBody toMessageBody(){
        return new EinzSpecifyRulesMessageBody(this.cardRules, this.globalRules);
    }

    public void addGlobalRuleWithoutParameters(String ruleName){
        this.addGlobalRuleWithParameters(ruleName, null);
    }
    

    public void addGlobalRule(BasicGlobalRule rule)
    public void addCardRule(String ruleName, String cardID);
    public void setNumberOfCards(String cardID)
}
