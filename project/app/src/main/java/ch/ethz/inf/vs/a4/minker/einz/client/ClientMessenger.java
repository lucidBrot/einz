package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import org.json.JSONException;

public class ClientMessenger implements EinzClientConnection.OnMessageReceived{

    // **preferences**
    // use the same parserfactory settings as the server because they could want to parse the same messages
    private int GAMELOGIC_PARSERMAPPINGS = R.raw.initial_game_logic_parser_mappings;
    private int NETWORKING_PARSERMAPPINGS = R.raw.initial_networking_parser_mappings;
    // use different actionfactory settings because the resulting action is probably different on the client side compared to the server
    private int GAMELOGIC_ACTIONMAPPINGS = R.raw.client_initial_game_logic_action_mappings;
    private int NETWORKING_ACTIONMAPPINGS = R.raw.client_initial_networking_action_mappings;

    // **class-wide fields**
    private EinzParserFactory parserFactory;
    private EinzActionFactory actionFactory;
    private Context appContext;
    private EinzClientConnection clientConnection;
    private ClientActionCallbackInterface actionsCallback;

    public ClientMessenger(Context appContext){
        // initialize local variables
        this.appContext = appContext;
        this.parserFactory = new EinzParserFactory();
        this.actionsCallback = new ClientMessengerCallback(); // TODO: specify this interface and implement it
        this.actionFactory = new EinzActionFactory(actionsCallback);

        // initialize Factories by loading the mappings from the specified files above
        initializeParserFactory(); // from messagegroup to Parser
        initializeActionFactory(); // from Message Object to Action Object

    }

    /**
     * Called by the {@link EinzClientConnection} on incoming messages
     * @param message
     */
    @Override
    public void messageReceived(String message) { // TODO: handle errors
        EinzParser parser = null; // get parser based on the messages messagegroup
        try {
            parser = parserFactory.generateEinzParser(message);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        EinzMessage einzMessage  = null; // get a message object
        try {
            einzMessage = parser.parse(message);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        EinzAction einzAction = actionFactory.generateEinzAction(einzMessage, null); // get action
        // einzAction is runnable. it contains the code that should handle this incoming message
        if(einzAction==null) {
            // invalid message is invalid af. Ignore
        }else {
            einzAction.run(); // TODO: write into the corresponding EinzAction subclasses what they should do on run()
            // maybe you'll want to call functions defined somewhere else, e.g. in here.
        }

    }

    private void initializeParserFactory() {
        try {
            this.parserFactory.loadMappingsFromResourceFile(appContext, NETWORKING_PARSERMAPPINGS);
            this.parserFactory.loadMappingsFromResourceFile(appContext, GAMELOGIC_PARSERMAPPINGS);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvalidResourceFormatException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeActionFactory() {
        try {
            this.actionFactory.loadMappingsFromResourceFile(appContext, NETWORKING_ACTIONMAPPINGS);
            this.actionFactory.loadMappingsFromResourceFile(appContext, GAMELOGIC_ACTIONMAPPINGS);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InvalidResourceFormatException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
