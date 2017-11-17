package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzUnregisterRequestMessageBody extends EinzMessageBody {
    public String getUsername() {
        return username;
    }

    private final String username;
    public EinzUnregisterRequestMessageBody(String username) {
        super();
        this.username = username;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return (new JSONObject()).put("username", username);
    }
}
