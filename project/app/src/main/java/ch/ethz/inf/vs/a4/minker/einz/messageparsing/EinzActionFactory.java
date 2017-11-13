package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import android.support.annotation.Nullable;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzPlayCardAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

import java.util.HashMap;

public class EinzActionFactory {

    private ServerFunctionDefinition sInterface;
    private HashMap<Class<? extends EinzMessageBody>, Class<? extends EinzAction>> dictionary; // mapping Message types to Action types
    // this Map is using the MessageBody because a) this is what distinguishes the messages and b) the generic type cannot really be gotten at runtime.
    // .getClass and .class only return EinzMessage, without the generic type

    public EinzActionFactory(ServerFunctionDefinition serverFunctionDefinition){
        this.sInterface = serverFunctionDefinition;
        this.dictionary = new HashMap<Class<? extends EinzMessageBody>, Class<? extends EinzAction>>();

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
    public void registerMapping(Class<? extends EinzMessageBody> bodyclass, Class<? extends EinzAction> actionclass){ // TODO: implement EinzActionCombination ?
        this.dictionary.put(bodyclass, actionclass);
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
     * @return
     */
    public Class<? extends EinzAction> getMapping(EinzMessage e){
        Log.d("DEBUG", "e.getBody().getClass() "+e.getBody().getClass());
        return this.dictionary.get(e.getBody().getClass());
    }

    public EinzAction<EinzPlayCardMessageBody> generateEinzAction(EinzMessage<EinzPlayCardMessageBody> message, @Nullable String issuedByPlayer){ // using type of message to determine action. Using overloading for this
        return new EinzPlayCardAction(sInterface, message, issuedByPlayer);
    }

    // TODO: implement for all other actions, including default if bad message
}
