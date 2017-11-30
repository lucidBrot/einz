package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzGameOverMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayerFinishedMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzEndGameParser extends EinzParser {
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch (messagetype) {
            case "PlayerFinished":
                return parsePlayerFinished(message);
            case "GameOver":
                return parseGameOver(message);
            default:
                Log.d("EinzEndGameParser", "Not a valid messagetype " + messagetype + " for EinzEndGameParser");
                return null;
        }
    }

    private EinzMessage parsePlayerFinished(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("endGame", "PlayerFinished");
        JSONObject body = message.getJSONObject("body");
        String username = body.getString("username");
        EinzMessageBody emb = new EinzPlayerFinishedMessageBody(username);
        //make message
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

    private EinzMessage parseGameOver(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("endGame", "GameOver");
        JSONObject body = message.getJSONObject("body");
        JSONObject pointsJSON = body.getJSONObject("points");
        HashMap<String, String> points = new HashMap<>();
        Iterator keys = pointsJSON.keys();
        while (keys.hasNext()) {
            String name = (String) keys.next();
            String num = pointsJSON.getString(name);
            points.put(name, num);
        }

        EinzMessageBody emb = new EinzGameOverMessageBody(points);
        //make message
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }
}
