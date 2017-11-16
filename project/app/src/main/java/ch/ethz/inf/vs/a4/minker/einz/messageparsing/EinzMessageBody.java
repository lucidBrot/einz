package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Message Body contains all information specific to the messagetype, i.e. the arguments for the corresponding EinzAction.
 * Any extension of this class can be used for a specific EinzParser's return value
 */
public abstract class EinzMessageBody {

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    public abstract JSONObject toJSON() throws JSONException;
}
