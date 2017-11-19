package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzGetStateMessageBody extends EinzMessageBody {

    public EinzGetStateMessageBody() {
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonBody = new JSONObject();
        return jsonBody;
    }
}
