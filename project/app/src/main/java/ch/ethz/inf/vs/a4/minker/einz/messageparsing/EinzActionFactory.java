package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.ClientActionCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzUnmappedAction;
import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler;
import ch.ethz.inf.vs.a4.minker.einz.server.EinzServerManager;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Scanner;

public class EinzActionFactory {

    private ServerFunctionDefinition sInterface;
    private HashMap<Class<? extends EinzMessageBody>, Class<? extends EinzAction>> dictionary; // mapping Message types to Action types
    // this Map is using the MessageBody because a) this is what distinguishes the messages and b) the generic text cannot really be gotten at runtime.
    // .getClass and .class only return EinzMessage, without the generic text
    // The key is EinzMessageBody and not just the messagtype because like this we could add further header info and create new EinzMessages based on them
    private EinzServerManager sManager;

    private EinzServerClientHandler clientHandler;
    private ClientActionCallbackInterface clientActionCallbackInterface;
    private Object completelyCustomObject;

    /**
     * This is the constructor that the server would be happy with. it does not need more
     * @param serverFunctionDefinition provides game logic actions
     * @param serverManager provides framework actions
     */
    public EinzActionFactory(ServerFunctionDefinition serverFunctionDefinition, EinzServerManager serverManager, EinzServerClientHandler clientHandler){
        this(serverFunctionDefinition, serverManager, clientHandler, null, null);

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
     * All options could be null, if the action supports that. The serverside actions need some of those, the clientside others, and further extensions may use the completelyCustom parameter
     * @param serverFunctionDefinition server
     * @param serverManager server
     * @param clientHandler server
     * @param clientCallbackInterface client
     * @param completelyCustom custom
     */
    public EinzActionFactory(ServerFunctionDefinition serverFunctionDefinition, EinzServerManager serverManager, EinzServerClientHandler clientHandler, ClientActionCallbackInterface clientCallbackInterface, Object completelyCustom){
        this.sInterface = serverFunctionDefinition;
        this.dictionary = new HashMap<Class<? extends EinzMessageBody>, Class<? extends EinzAction>>();
        this.sManager = serverManager;
        this.clientHandler = clientHandler;
        this.completelyCustomObject = completelyCustom;
        this.clientActionCallbackInterface = clientCallbackInterface;
    }

    /**
     * This constructor is enough for the client.
     * Sets every other option to null. Make sure the actions that you map to don't need them!
     * @param clientCallbackInterface
     */
    public EinzActionFactory(ClientActionCallbackInterface clientCallbackInterface){
        this(null, null, null, clientCallbackInterface, null);
    }

    /**
     * sets every other option to null. Make sure the actions that you map to support this!
     * @param customObject
     */
    public EinzActionFactory(Object customObject){
        this(null, null, null, null, customObject);
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
     * get the action text that is currently mapped to this messagetype
     * @param bodyclass the class of the body of the EinzMessage
     * @return
     */
    public Class<?extends EinzAction> getMapping(Class <?extends EinzMessageBody> bodyclass){
        return this.dictionary.get(bodyclass);
    }

    /**
     * more convenient interface than with the bodytype as parameter.
     * Takes an EinzMessage and returns its mapping.
     * This does not keep the object, only its text!
     * @param e
     * @return null if mapping does not exist, else the Class you want
     */
    public Class<? extends EinzAction> getMapping(EinzMessage e){
        if(Debug.logKeepalivePackets || !e.getBody().getClass().toString().equals("class ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKeepaliveMessageBody"));
        Log.d("ActionFactory", "Getting mapping for body text "+e.getBody().getClass());
        Class temp = this.dictionary.get(e.getBody().getClass());
        if(temp == null) {Log.d("ActionFactory", "Mapping was requested but not registered before");}
        return temp;
    }


    /**
     * This can fail in <b>so</b> many ways
     * @param message some Message that is the key to the Action
     * @param issuedBy the username who issued this action or null if irrelevant and unknown.
     *                 The server-side has multiple connections and thus needs this field. the client probably doesn't care
     * @return the action, (or null, but I don't think so, if this messagetype was not registered or it failed for some other reason, or maybe just the empty {@link ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzUnmappedAction})
     */
    @Nullable
    public EinzAction generateEinzAction(EinzMessage message, @Nullable String issuedBy){
        if(message == null){
            Log.e("ActionFactory", "Message was null.");
            return new EinzUnmappedAction(sInterface, sManager, message, issuedBy, clientHandler, clientActionCallbackInterface, completelyCustomObject);
        }
        try {
            Class<? extends  EinzAction> mapping = getMapping(message);
            if(mapping == null){
                Log.w("ActionFactory", "generation of unregistered action was requested!");
                Log.d("ActionFactory", "The unregistered action was for "+message.toJSON().toString());
                return new EinzUnmappedAction(sInterface, sManager, message, issuedBy, clientHandler);
            }

            EinzAction ret = mapping.getDeclaredConstructor(ServerFunctionDefinition.class, EinzServerManager.class, message.getClass(), String.class, EinzServerClientHandler.class, ClientActionCallbackInterface.class, Object.class).newInstance(sInterface, sManager, message, issuedBy, this.clientHandler, this.clientActionCallbackInterface, this.completelyCustomObject);

            if(Debug.logKeepalivePackets || !ret.getClass().toString().equals("class ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzKeepaliveAction")) {
                // don't log this if this is a keepalive packet unless logging is activated in Debug.java
                Log.d("ActionFactory", "successfully generated action of text " + ret.getClass());
            }

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
        } catch (JSONException e) {
            e.printStackTrace();
            // When printing the unregistered action fails. Whatever, don't care. Action is unregistered.
        }
        Log.w("ActionFactory", "failed to map to an action");
        return new EinzUnmappedAction(sInterface, sManager, message, issuedBy, clientHandler);
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
                Log.d("ActionFactory/load","loading class mapping for : "+substring);
                Class o = Class.forName(substring);
                if (!(EinzAction.class.isAssignableFrom(o))) { // read the docs of isAssignableFrom. I'm testing if o is an EinzParser or a subclass thereof
                    throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"mapstoaction\" is not of text Class");
                } else {
                    // everything is fine, do stuff
                    @SuppressWarnings("unchecked") // I checked this with above tests
                            Class<? extends EinzAction> actionclass = (Class<? extends EinzAction>) o;

                    // for the dictionary, we need the key also as a class
                    substring = pair.getString("messagebodyclass").substring(prefix.length());
                    if(!s.startsWith(prefix)){
                        throw (new InvalidResourceFormatException()).extendMessageInline("Some object within the JSON Array \"actionmappings\" is not of text Class");
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

    public void loadMappingsFromResourceFile(Context applicationContext, int resourceFile) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        InputStream jsonStream = applicationContext.getResources().openRawResource(resourceFile);
        JSONObject jsonObject = new JSONObject(convertStreamToString(jsonStream));
        this.loadMappingsFromJson(jsonObject);
    }

    // https://stackoverflow.com/questions/6774579/typearray-in-android-how-to-store-custom-objects-in-xml-and-retrieve-them
    // utility function
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is);
        s.useDelimiter("\\A");
        String ret = s.hasNext() ? s.next() : "";
        s.close();
        return ret;
    }
}
