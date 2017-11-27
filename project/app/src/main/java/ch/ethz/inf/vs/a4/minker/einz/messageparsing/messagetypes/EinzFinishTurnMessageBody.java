package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/24/17.
 */

public class EinzFinishTurnMessageBody extends EinzMessageBody {
    @Override
    public JSONObject toJSON() throws JSONException {
        return new JSONObject();
    }
}
/*
{
  "header":{
    "messagegroup":"furtheractions",
    "messagetype":"FinishTurn"
  },
  "body":{}
}
 */
