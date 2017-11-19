package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzPlayCardResponseMessageBody extends EinzMessageBody {

    private final String success;

    public EinzPlayCardResponseMessageBody(String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }


    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", this.getSuccess());
        return jsonObject;
    }
}
/*
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlaycardResponse"
  },
  "body":{
	"success":"true"
  }
}
 */