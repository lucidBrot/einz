package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSendStateMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzShowToastMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzToastParser extends EinzParser {
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch (messagetype) {
            case "ShowToast":
                return parseShowToast(message);
            default:
                Log.d("EinzToastParser", "Not a valid messagetype " + messagetype + " for EinzToastParser");
                return null;
        }
    }

    private EinzMessage parseShowToast(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("toast", "ShowToast");
        JSONObject body = message.getJSONObject("body");
        String toast = body.getString("toast");
        String from = body.getString("from");
        JSONObject styleJSON = body.getJSONObject("style");
        HashMap<String, String> style = new HashMap<>();
        Iterator keys = styleJSON.keys();
        while (keys.hasNext()) {
            String property = (String) keys.next();
            String value = styleJSON.getString(property);
            style.put(property, value);
        }

        //make messageBody
        EinzMessageBody emb = new EinzShowToastMessageBody(toast, from, style);
        //make message
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;

    }
}
