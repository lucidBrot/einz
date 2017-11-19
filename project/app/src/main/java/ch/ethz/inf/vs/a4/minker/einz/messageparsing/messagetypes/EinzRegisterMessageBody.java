package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzRegisterMessageBody extends EinzMessageBody {

    private final String username;
    private final String role;

    public EinzRegisterMessageBody(String username, String role) {
        this.username = username;
        this.role = role;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("username", username);
        jsonBody.put("role", role);
        return jsonBody;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
