package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import org.json.JSONObject;

/**
 * Represents all body info as JSON. Seemed easiest to me but may turn out to be retarded.
 * If it is retarded, just use other extensions of EinzMessageBody
 */
public class EinzJsonMessageBody extends EinzMessageBody {
    private JSONObject body;

    public EinzJsonMessageBody(JSONObject body){
        this.body = body;
    }

    public JSONObject getBody() {
        return body;
    }
}
