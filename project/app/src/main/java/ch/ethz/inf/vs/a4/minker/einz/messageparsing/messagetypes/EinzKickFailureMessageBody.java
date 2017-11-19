package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzKickFailureMessageBody extends EinzMessageBody {
    private final String username, reason;

    public EinzKickFailureMessageBody(String username, String reason) {
        this.username = username;
        this.reason = reason;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("username", username);
        body.put("reason", reason);
        return body;
    }
}
