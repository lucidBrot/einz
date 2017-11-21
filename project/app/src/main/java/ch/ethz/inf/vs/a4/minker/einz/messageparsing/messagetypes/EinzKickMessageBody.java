package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzKickMessageBody extends EinzMessageBody{

    private final String username;

    /**
     * @param whotokick the username
     */
    public EinzKickMessageBody (String whotokick){
        this.username = whotokick;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        return (new JSONObject()).put("username" , this.username);
    }

    public String getUsername() {
        return username;
    }
    /*
        {
      "header":{
        "messagegroup":"registration",
        "messagetype":"Kick"
      },
      "body":{
        "username":"that random dude who we didn't want",
      }
    }
     */

}
