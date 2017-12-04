package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKeepAliveMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzNetworkingParser extends EinzParser{
    /**
     * @param message JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessage Object containing all the information specific to this kind of message
     */
    @Override
    public EinzMessage<? extends EinzMessageBody> parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch (messagetype) {
            case "KeepAlive":
                return parseKeepAlive(message);
            default:
                Log.d("FurtherActionsParser", "Not a valid messagetype " + messagetype + " for EinzNetworkingParser");
                return null;
                //TODO: implement ping/pong
        }
    }

    private EinzMessage<EinzKeepAliveMessageBody> parseKeepAlive(JSONObject message) {
        EinzMessageHeader header = new EinzMessageHeader("networking", "keepalive");
        EinzKeepAliveMessageBody body = new EinzKeepAliveMessageBody();
        return new EinzMessage<>(header, body);
    }
}
