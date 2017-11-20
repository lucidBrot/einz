package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzDrawCardsFailureMessageBody extends EinzMessageBody {
    private final String reason;

    public EinzDrawCardsFailureMessageBody(String reason) {
        this.reason = reason;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("reason", reason);
        return body;
    }

    public String getReason() {
        return reason;
    }
}
