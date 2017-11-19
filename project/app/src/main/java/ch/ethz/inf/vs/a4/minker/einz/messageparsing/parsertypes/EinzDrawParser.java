package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzDrawParser extends EinzParser {

    /**
     * @param message JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessage Object containing all the information specific to this kind of message
     */
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        JSONObject header = message.getJSONObject("header");
        switch(header.getString("messagegroup")){
            case "DrawCardsResponse":
                return parseDrawCardsResponse(message);
            case "DrawCards":
                return parseDrawCards(message);
            default:
                Log.d("EinzDrawParser","Not a valid messagetype "+header.getString("messagetype")+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage parseDrawCards(JSONObject message) throws JSONException {
        return new EinzMessage<EinzDrawCardsMessageBody>(
                new EinzMessageHeader("draw", "DrawCards"),
                new EinzDrawCardsMessageBody()
        );
    }
}
