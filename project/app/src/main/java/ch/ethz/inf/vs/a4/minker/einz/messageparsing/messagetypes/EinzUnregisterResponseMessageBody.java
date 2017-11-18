package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzUnregisterResponseMessageBody extends EinzMessageBody {

    private final String username, reason;

    public EinzUnregisterResponseMessageBody(String username, String reason){
        this.username = username;

        this.reason = reason;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        return new JSONObject("{\"username\":\""+this.username+"\"," +
                "\"reason\":\""+this.reason+"\"}");
    }

    public String getReason() {
        return reason;
    }

    public String getUsername() {
        return username;
    }
}
