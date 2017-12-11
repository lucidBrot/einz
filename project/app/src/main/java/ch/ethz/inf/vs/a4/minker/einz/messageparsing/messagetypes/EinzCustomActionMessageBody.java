package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/24/17.
 */

public class EinzCustomActionMessageBody extends EinzMessageBody {

    final JSONObject ruleParameterBody;
    final String ruleName;

    public EinzCustomActionMessageBody(JSONObject ruleParameterBody, String ruleName) {
        this.ruleParameterBody = ruleParameterBody;
        this.ruleName = ruleName;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return this.ruleParameterBody;
    }

    public JSONObject getRuleParameterBody() {
        return ruleParameterBody;
    }

    public String getRuleName() {
        return ruleName;
    }
}
/*
{
  "header":{
    "messagegroup":"furtheractions",
    "messagetype":"CustomAction"
  },
  "body":{
    "custom parameter of the rule":{ a custom JSONObject},
  }
}
 */
