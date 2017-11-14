package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.HashMap;

/**
 * This class is used to generate an EinzParser that corresponds to the messagegroup.
 * Different messagetypes will be distinguished within the EinzParser.
 * This simplifies maintaining the code because to change one messagegroup, you only have to change that EinzParser
 */
public class EinzParserFactory {

    private HashMap<String, Class<? extends EinzParser>> dictionary; // Map "messagegroup" to the class of some EinzParser

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
     * This function is basically a mapping from the messagegroup to the corresponding parser.
     * The parser is a new instance but it can be reused by you.
     * @param messagegroup can be one of the following:
     *                     "registration"
     *                     none of the above -> returns a default parser
     *
     * @return Some EinzParser specific to the messagegroup
     */
    private EinzParser getMatchingParser(String messagegroup){

        return getMappedInstance(messagegroup);
    }

    /**
     *
     * @param messagegroup String identifier for message group
     * @return a subclass of EinzParser or null if no mapping registered
     */
    private Class<? extends EinzParser> getMapping(String messagegroup){
        return this.dictionary.get(messagegroup);
    }

    /**
     * Get EinzParser for this messagegroup
     * @param messagegroup String identifier for message group
     * @return null if failed or parser not registered
     */
    private EinzParser getMappedInstance(String messagegroup){
        try {
            Class<? extends EinzParser> c = getMapping(messagegroup);
            if(c==null)
                return null;

            return getMapping(messagegroup).getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e("EinzParserFactory", "Failed to instantiate mapped Parser");
            e.printStackTrace();
        }
        Log.e("EinzParserFactory", "Failed to instantiate mapped Parser");
        return null;
    }

    /**
     * @param messagegroup The key. A String that is contained as messagegroup in the Message header
     * @param parserclass The value. The Class of some EinzParser
     */
    public void registerMessagegroup(String messagegroup, Class<? extends EinzParser> parserclass){
        this.dictionary.put(messagegroup, parserclass);
    }

    /**
     * @param messagegroup The messagegroup whose mapping should be removed from the internal dictionary.
     */
    public void deregisterMessagegroup(String messagegroup){ // TODO: what does this do if the messagegroup wasn't registered?
        this.dictionary.remove(messagegroup);
    }
}
