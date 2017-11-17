package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.support.annotation.Nullable;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzPlayCardAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class EinzActionFactory {

    private ServerFunctionDefinition sInterface;
    private HashMap<Class<? extends EinzMessageBody>, Class<? extends EinzAction>> dictionary; // mapping Message types to Action types
    // this Map is using the MessageBody because a) this is what distinguishes the messages and b) the generic type cannot really be gotten at runtime.
    // .getClass and .class only return EinzMessage, without the generic type
    // The key is EinzMessageBody and not just the messagtype because like this we could add further header info and create new EinzMessages based on them
    private EinzServerManager sManager;

    private EinzServerClientHandler clientHandler;

    /**
     * @param serverFunctionDefinition provides game logic actions
     * @param serverManager provides framework actions
     */
    public EinzActionFactory(ServerFunctionDefinition serverFunctionDefinition, EinzServerManager serverManager, EinzServerClientHandler clientHandler){
        this.sInterface = serverFunctionDefinition;
        this.dictionary = new HashMap<Class<? extends EinzMessageBody>, Class<? extends EinzAction>>();
        this.sManager = serverManager;
        this.clientHandler = clientHandler;

        //<Debug>
        /*
        EinzMessage ezm = new EinzMessage(new EinzMessageHeader("alpha", "beta"), new EinzPlayCardMessageBody());
        this.dictionary.put(ezm.getBody().getClass(), EinzPlayCardAction.class);
        Log.d("DEBUG", "ezm.getClass: "+ezm.getClass()+" \t EinzMessage.class: "+EinzMessage.class+"\n\t EinzPlayCardMessageBody.class: "+EinzPlayCardMessageBody.class);
        */
        //getMapping(new EinzMessage(new EinzMessageHeader("a", "b"), new EinzPlayCardMessageBody()));
        //</Debug>
    }

    /**
     * register a mapping from a messagetype to an action. Because the message's type can only be gotten via its body, you need to pass the class of the body.
     * If the entry already exists, this will replace it. (If you want to execute both the old and the new action, you need to pass that as one action)
     */
    public void registerMapping(Class<? extends EinzMessageBody> bodyclass, Class<? extends EinzAction> actionclass){
        this.dictionary.put(bodyclass, actionclass);
    }

    public void deregisterMapping(Class<? extends EinzMessageBody> bodyclass){
        this.dictionary.remove(bodyclass);
    }

    /**
     * This does not store the objects, only their classes!
     * @param message message.body.getClass will be kept as key
     * @param action action.getClass will be kept as resulting actiontype to be created when the key is given
     * To pass a custom Action, extend EinzAction and pass an instance of it (or use the other method where you can pass its class instead)
     */
    public void registerMapping(EinzMessage message, EinzAction action ){
        this.dictionary.put(message.getBody().getClass(), action.getClass());
    }

    /**
     * get the action type that is currently mapped to this messagetype
     * @param bodyclass the class of the body of the EinzMessage
     * @return
     */
    public Class<?extends EinzAction> getMapping(Class <?extends EinzMessageBody> bodyclass){
        return this.dictionary.get(bodyclass);
    }

    /**
     * more convenient interface than with the bodytype as parameter.
     * Takes an EinzMessage and returns its mapping.
     * This does not keep the object, only its type!
     * @param e
     * @return null if mapping does not exist, else the Class you want // TODO: Default Action?
     */
    public Class<? extends EinzAction> getMapping(EinzMessage e){
        Log.d("ActionFactory", "Getting mapping for body type "+e.getBody().getClass());
        Class temp = this.dictionary.get(e.getBody().getClass());
        if(temp == null) {Log.d("ActionFactory", "Mapping was requested but not registered before");}
        return temp;
    }

    /**
     * This can fail in <b>so</b> many ways
     * @param message some Message that is the key to the Action
     * @param issuedBy the username who issued this action or null if irrelevant and unknown
     * @return the action, or null if this messagetype was not registered or it failed for some other reason
     */
    public EinzAction generateEinzAction(EinzMessage message, @Nullable String issuedBy){
        if(message == null){
            Log.e("ActionFactory", "Message was null.");
            return null;
        }
        try {
            EinzAction ret = getMapping(message).getDeclaredConstructor(ServerFunctionDefinition.class, EinzServerManager.class, message.getClass(), String.class, EinzServerClientHandler.class).newInstance(sInterface, sManager, message, issuedBy, this.clientHandler);
            Log.d("ActionFactory","successfully generated action of type "+ret.getClass());
            return ret;
        } catch (InstantiationException e) {
            Log.e("ActionFactory", "Failed to generate Mapping: "+e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e("ActionFactory", "Failed to generate Mapping: "+e.getMessage());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Log.e("ActionFactory", "Failed to generate Mapping: "+e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e("ActionFactory", "Failed to generate Mapping: "+e.getMessage());
            e.printStackTrace();
        }
        Log.e("ActionFactory", "failed to map to an action");
        return null;
    }

    /**
     * Loads every mapping in the JSONObject and store it in this Factories' dictionary (the below example might not be defined like this)
     * @param messageToParsermappings
     *      a JSONObject of format
     * {
     * "actionmappings":[
     *   {"messagebodyclass":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzJsonMessageBody","mapstoaction":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegisterAction"},
     *   {"messagebodyclass":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzPlayCardActionMessageBody","mapstoaction":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzDrawCardsAction"}
     *  ]
     * }
     *
     * Multiple keys of the same Class mean that only the last entry will be kept
     * @throws JSONException If the JSONObject is not valid. i.e. it does not contain the "messagebodyclass" and "messagetype"s within them.
     * @throws InvalidResourceFormatException If the JSON does not start with the prefix "class ". I might add other reasons later
     * @throws ClassNotFoundException If the stored class mapping in the json file does not exist
     */
    public void loadMappingsFromJson(JSONObject messageToParsermappings) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        JSONArray array = messageToParsermappings.getJSONArray("actionmappings");
        int size = array.length();
        // register each object
        for (int i = 0; i < size; i++) {
            JSONObject pair = array.getJSONObject(i);
            String s = pair.getString("mapstoaction");
            String prefix = "class ";
            if (!s.startsWith(prefix)) {
                throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"mapstoaction\" does not start with class ");
            } else {
                String substring = s.substring(prefix.length()); // classname without prefix
                Log.d("ActionFactory","substring to get class of : "+substring);
                Class o = Class.forName(substring); // was substring
                if (!(EinzAction.class.isAssignableFrom(o))) { // read the docs of isAssignableFrom. I'm testing if o is an EinzParser or a subclass thereof
                    throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"mapstoaction\" is not of type Class");
                } else {
                    // everything is fine, do stuff
                    @SuppressWarnings("unchecked") // I checked this with above tests
                            Class<? extends EinzAction> actionclass = (Class<? extends EinzAction>) o;

                    // for the dictionary, we need the key also as a class
                    substring = pair.getString("messagebodyclass").substring(prefix.length());
                    if(!s.startsWith(prefix)){
                        throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"actionmappings\" is not of type Class");
                    } else {
                        Class q = Class.forName(substring);
                        // everything is fine, do stuff
                        @SuppressWarnings("unchecked") // I checked this with above tests
                                Class<? extends EinzMessageBody> messagebodyclass = (Class<? extends EinzMessageBody>) q;

                        registerMapping(messagebodyclass, actionclass);
                    }
                }
            }
        }
    }
}
