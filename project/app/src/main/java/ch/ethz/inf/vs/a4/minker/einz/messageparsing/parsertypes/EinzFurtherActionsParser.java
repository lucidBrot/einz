package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionResponseMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzFinishTurnMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayerFinishedMessageBody;

/**
 * Created by silvia on 11/24/17.
 */

public class EinzFurtherActionsParser extends EinzParser {
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch (messagetype) {
            case "CustomAction":
                return parseCustomAction(message);
            case "CustomActionResponse":
                return parseCustomActionResponse(message);
            case "FinishTurn":
                return parseFinishTurn(message);
            default:
                Log.d("FurtherActionsParser", "Not a valid messagetype " + messagetype + " for EinzFurtherActionsParser");
                return null;
        }
    }

    private EinzMessage parseCustomAction(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("furtheractions", "CustomAction");
        JSONObject body = message.getJSONObject("body");
        EinzMessageBody emb = new EinzCustomActionMessageBody(body);
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

    private EinzMessage parseCustomActionResponse(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("furtheractions", "CustomActionResponse");
        JSONObject body = message.getJSONObject("body");
        EinzMessageBody emb = new EinzCustomActionResponseMessageBody(body);
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

    private EinzMessage parseFinishTurn(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("furtheractions", "FinishTurn");
        EinzMessageBody emb = new EinzFinishTurnMessageBody();
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;

    }
}
