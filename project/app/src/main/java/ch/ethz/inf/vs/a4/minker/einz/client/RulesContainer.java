package ch.ethz.inf.vs.a4.minker.einz.client;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;
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

    public void addGlobalRule(BasicGlobalRule rule){
        String id = rule.getName();
        JSONObject params = null;
        if(rule instanceof ParametrizedRule){params = ((ParametrizedRule) rule).getParameter();}
    }
    public void addCardRule(String ruleName, String cardID);
    public void setNumberOfCards(String cardID)
}
