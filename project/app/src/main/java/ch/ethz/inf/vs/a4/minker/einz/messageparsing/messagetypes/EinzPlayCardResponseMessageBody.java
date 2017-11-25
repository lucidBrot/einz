package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzPlayCardResponseMessageBody extends EinzMessageBody {

    private String success;

    public EinzPlayCardResponseMessageBody(String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }


    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        String success = this.getSuccess();
        jsonObject.put("success", success);
        return jsonObject;
    }
}
/*
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlayCardResponse"
  },
  "body":{
	"success":"true"
  }
}
 */