package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

/**
 * Specifies that an EinzAction should have a function run which will run the action
 */
public abstract class EinzAction<MESSAGEBODYTYPE  extends EinzMessageBody> {

    /**
     * executes the action in the current thread
     * @param täter The player who is requesting the change, as this is never included in the message itself
     */
    public abstract void run(Player täter);

    protected ServerFunctionDefinition sInterface;
    protected EinzMessage<MESSAGEBODYTYPE> message; // This has a specific messagebody in every subclass

    public EinzAction(ServerFunctionDefinition sInterface, EinzMessage<MESSAGEBODYTYPE> params){
        this.sInterface = sInterface;
        this.message = params;
    }
}
