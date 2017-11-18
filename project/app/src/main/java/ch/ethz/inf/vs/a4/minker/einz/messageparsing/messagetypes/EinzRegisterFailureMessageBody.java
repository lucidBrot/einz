package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzRegisterFailureMessageBody extends EinzMessageBody{

    private final String role;

    public String getRole() {
        return role;
    }

    public String getUsername() {
        return username;
    }

    public String getReason() {
        return reason;
    }

    private final String username;
    private final String reason;

    public EinzRegisterFailureMessageBody(String role, String username, String reason){
        /*{
            "header":{
            "messagegroup":"registration",
                    "messagetype":"RegisterFailure"
        },
            "body":{
            "role":"player",
                    "username":"server",
                    "reason":"invalid"
        }
        }*/
        this.role = role;
        this.username = username;
        this.reason = reason;
    }
    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("role", role);
        body.put("username", username);
        body.put("reason", reason);
        return body;
    }
}
