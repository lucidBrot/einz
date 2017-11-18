package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzPlayerFinishedMessageBody extends EinzMessageBody {

    private String username;

    public EinzPlayerFinishedMessageBody(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return null;
    }
}
/*
{
  "header":{
    "messagegroup":"endGame",
    "messagetype":"PlayerFinished"
  },
  "body":{
    "username":"roger",
  }
}
 */
