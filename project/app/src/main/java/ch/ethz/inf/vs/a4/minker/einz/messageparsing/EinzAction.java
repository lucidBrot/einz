package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunctionDefinition;

/**
 * Specifies that an EinzAction should have a function run which will run the action
 */
public abstract class EinzAction<MESSAGEBODYTYPE  extends EinzMessageBody> {

    /**
     * executes the action in the current thread
     */
    public abstract void run();

    protected ServerFunctionDefinition sInterface;
    protected EinzMessage message; // This has a specific messagebody in every subclass
    protected String issuedByPlayer; // CAN BE NULL if user is not registered or not known and irrelevant

    public EinzAction(ServerFunctionDefinition sInterface, EinzMessage params, String issuedByPlayer){
        this.sInterface = sInterface;
        this.message = params;
        this.issuedByPlayer = issuedByPlayer;
    }
}
