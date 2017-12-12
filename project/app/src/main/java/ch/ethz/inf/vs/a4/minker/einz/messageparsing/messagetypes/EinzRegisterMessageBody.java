package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzRegisterMessageBody extends EinzMessageBody {

    private final String username;
    private final String role;
    private final JSONObject playerSeating; // used for spectators displaying positioning of players

    public EinzRegisterMessageBody(String username, String role, JSONObject playerSeating) {
        this.username = username;
        this.role = role;
        this.playerSeating = playerSeating;
    }

    public EinzRegisterMessageBody(String username, String role) {
        this(username, role, new JSONObject());
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("username", username);
        jsonBody.put("role", role);
        jsonBody.put("playerSeating", playerSeating);
        return jsonBody;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public JSONObject getPlayerSeating() {
        return playerSeating;
    }
}
