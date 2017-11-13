package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class EinzParser {

    /**
     * Calls parse(JSONObject).
     * @param message String of a JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessage Object containing all the information specific to this kind of message.
     */
    public EinzMessage parse (String message) throws JSONException{
        JSONObject msg = null;
        try {
            msg = new JSONObject(message);
        } catch (JSONException e) {
            // not a valid JSON Object
            Log.w("EinzParser", "Failed to generate Parser. Message is not valid JSONObject: "+message);
            throw e;
        }
        return parse(msg);
    }

    /**
     * Calls parseHeader(JSONObject)
     * @param message String of a JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessageHeader Object. This is uniform across all messages
     */
    public EinzMessageHeader parseHeader (String message) throws JSONException{
        JSONObject msg = null;
        try {
            msg = new JSONObject(message);
        } catch (JSONException e) {
            // not a valid JSON Object
            Log.w("EinzParser", "Failed to generate Parser. Message is not valid JSONObject: "+message);
            throw e;
        }
        return parseHeader(msg);
    }

    /**
     * @param message JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessage Object containing all the information specific to this kind of message
     */
    public abstract EinzMessage parse (JSONObject message) throws JSONException;


    /**
     * @param message SON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessageHeader Object. This is uniform across all messages
     */
    public abstract EinzMessageHeader parseHeader (JSONObject message) throws JSONException;

}
