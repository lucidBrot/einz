package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Dictionary;
import java.util.HashMap;

/**
 * This class is used to generate an EinzParser that corresponds to the messagegroup.
 * Different messagetypes will be distinguished within the EinzParser.
 * This simplifies maintaining the code because to change one messagegroup, you only have to change that EinzParser
 */
public class EinzParserFactory {

    /**
     * Might return null if message is not a valid JSONObject or not a valid message
     * @param message String representation of a message as specified in protocols/documentation_Messages.md
     * @return Parser specifically for this messagegroup
     * @throws JSONException if the message is invalidly formatted. E.g. if it is not a JSONObject or if it has no header...
     */
    public EinzParser generateEinzParser(String message) throws JSONException {
        JSONObject msg = null;
        try {
            msg = new JSONObject(message);
        } catch (JSONException e) {
            // not a valid JSON Object
            Log.w("EinzParserFactory", "Failed to generate Parser. Message is not valid JSONObject: "+message);
            throw e;
        }

        return generateEinzParser(msg);
    }


    /**
     * Might return null if message is not a valid message
     * @param message JSON representation of a message as specified in protocols/documentation_Messages.md
     * @return Parser specifically for this messagegroup
     * @throws JSONException if the message is invalidly formatted. E.g. if it is not a JSONObject or if it has no header etc.
     */
    public EinzParser generateEinzParser(JSONObject message) throws JSONException {
        JSONObject header = message.getJSONObject("header");
        String messagegroup = header.getString("messagegroup");

        return getMatchingParser(messagegroup);
    }

    /**
     * This function is basically a mapping from the messagegroup to the corresponding parser
     * @param messagegroup can be one of the following:
     *                     "registration"
     *                     none of the above -> returns a default parser
     * @return Some EinzParser specific to the messagegroup
     */
    private EinzParser getMatchingParser(String messagegroup){

        switch(messagegroup){ // TODO: add all messagegroups to mapping, and do so with a dictionary from messagegroup to parser instead
            case "registration":
                return new EinzRegistrationParser();
            default:
                Log.d("EinzParserFactory","No matching Parsertype for messagegroup "+messagegroup);
                // TODO: return null or defaultparser ?
                break;
        }
        return null;
    }
}
