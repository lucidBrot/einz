package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/24/17.
 */

public class EinzCustomActionMessageBody extends EinzMessageBody {

    JSONObject ruleParameter;

    public EinzCustomActionMessageBody(JSONObject ruleParameter) {
        this.ruleParameter = ruleParameter;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return this.ruleParameter;
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
