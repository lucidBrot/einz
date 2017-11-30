package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzUnmappedMessageBody extends EinzMessageBody {

    private final JSONObject message;
    public EinzUnmappedMessageBody(JSONObject message){
        this.message = message;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return (new JSONObject()).put("message", message);
    }
}
