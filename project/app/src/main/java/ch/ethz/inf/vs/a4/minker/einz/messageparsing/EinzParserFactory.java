package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzUnmappedParser;
import org.json.JSONArray;
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

    public EinzParserFactory(){
        this.dictionary = new HashMap<>();
    }

    /**
     * Might return null if message is not a valid JSONObject or not a valid message.
     * Will return EinzUnmappedParser if the message was kinda valid but not mapped
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
                return new EinzUnmappedParser(); // in case it is not mapped

            return getMapping(messagegroup).getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            Log.e("EinzParserFactory", "Failed to instantiate mapped Parser");
            e.printStackTrace();
        }
        Log.e("EinzParserFactory", "Failed to instantiate mapped Parser");
        return new EinzUnmappedParser();
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
     *                     Does nothing if the messagegroup is not registered
     */
    public void deregisterMessagegroup(String messagegroup){
        this.dictionary.remove(messagegroup);
    }

    /**
     * Loads every mapping in the JSONObject and store it in this Factories' dictionary
     * @param messageToParsermappings
     *      a JSONObject of format
     * {
     * "parsermappings":[
     *   {"messagegroup":"registration","mapstoparser":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser"},
     *   {"messagegroup":"draw","mapstoparser":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzDrawParser"}
     *  ]
     * }
     * @throws JSONException If the JSONObject is not valid. i.e. it does not contain the "parsermappings" and "messagegroups"s within them.
     * @throws InvalidResourceFormatException If the JSON does not start with the prefix "class ". I might add other reasons later
     * @throws ClassNotFoundException If the stored class mapping in the json file does not exist
     */
    public void loadMappingsFromJson(JSONObject messageToParsermappings) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        JSONArray array = messageToParsermappings.getJSONArray("parsermappings");
        int size = array.length();
        // register each object
        for(int i=0; i<size; i++){
            JSONObject pair = array.getJSONObject(i);
            String s =pair.getString("mapstoparser");
            String prefix = "class ";
            if(!s.startsWith(prefix)){
                throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"parsermappings\" does not start with class ");
            } else {
                String substring = s.substring(prefix.length()); // classname without prefix
                Class o = Class.forName(substring);
                if (!(EinzParser.class.isAssignableFrom(o))) { // read the docs of isAssignableFrom. I'm testing if o is an EinzParser or a subclass thereof
                    throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"parsermappings\" is not of type Class");
                } else {
                    // everything is fine, do stuff
                    @SuppressWarnings("unchecked") // I checked this with above tests
                            Class<? extends EinzParser> parserclass = (Class<? extends EinzParser>) o;
                    this.registerMessagegroup(pair.getString("messagegroup"), parserclass);
                }
            }
        }
    }
}
