package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/24/17.
 */

public class EinzCustomActionResponseMessageBody extends EinzMessageBody {

    final JSONObject ruleParameter;
    final String ruleName;
    final boolean success;

    public EinzCustomActionResponseMessageBody(JSONObject ruleParameter, String ruleName, String success) {
        this.ruleParameter = ruleParameter;
        this.ruleName = ruleName;
        this.success = success.equals("true");
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return this.ruleParameter;
    }

    public JSONObject getRuleParameter() {
        return ruleParameter;
    }

    public String getRuleName() {
        return ruleName;
    }

    public boolean isSuccess() {
        return success;
    }
}
/*
{
  "header":{
    "messagegroup":"furtheractions",
    "messagetype":"CustomActionResponse"
  },
  "body":{
    "custom parameter of the rule":{ a custom JSONObject},
  }
}
 */