package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzRegisterSuccessMessageBody extends EinzMessageBody {

    /*
    {
        "header":{
        "messagegroup":"registration",
                "messagetype":"RegisterSuccess"
    },
        "body":{
        "username":"roger",
                "role":"spectator",
    }
    }
    */

    private final String username, role;

    public EinzRegisterSuccessMessageBody(String username, String role){

        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        body.put("role", role);
        body.put("username", username);
        return body;
    }
}
