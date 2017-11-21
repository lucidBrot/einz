package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzGetStateMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSendStateMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzStateInfoParser extends EinzParser {
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch (messagetype) {
            case "SendState":
                return parseSendState(message);
            case "GetState":
                return parseGetState(message);
            default:
                Log.d("EinzStateInfoParser", "Not a valid messagetype " + messagetype + " for EinzStateInfoParser");
                return null;
        }
    }

    private EinzMessage parseSendState(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("stateinfo", "SendState");
        JSONObject body = message.getJSONObject("body");

        //get globalstate
        JSONObject globalstateJSON = body.getJSONObject("globalstate");
        JSONObject numcardsinhandJSON = globalstateJSON.getJSONObject("numcardsinhand");
        HashMap<String, Integer> numcardsinhand = new HashMap();
        Iterator keys = numcardsinhandJSON.keys();
        while (keys.hasNext()) {
            String name = (String) keys.next();
            String numString = numcardsinhandJSON.getString(name);
            Integer num = Integer.getInteger(numString);
            numcardsinhand.put(name, num);
        }
        JSONArray stackJSON = globalstateJSON.getJSONArray("stack");
        ArrayList<Card> stack = new ArrayList<>();
        for (int i = 0; i < stackJSON.length(); i++) {
            JSONObject cardJSON = stackJSON.getJSONObject(i);
            String ID = cardJSON.getString("ID");
            String origin = cardJSON.getString("origin");
            Card card = new Card(ID, origin);
            stack.add(card);
        }
        String whoseturn = globalstateJSON.getString("whoseturn");
        String drawxcardsmin = globalstateJSON.getString("drawxcardsmin");
        GlobalState globalstate = new GlobalState(numcardsinhand, stack, whoseturn, drawxcardsmin);

        //get playerstate
        JSONObject playerstateJSON = body.getJSONObject("playerstate");
        JSONArray handJSON = playerstateJSON.getJSONArray("hand");
        ArrayList<Card> hand = new ArrayList<>();
        for (int i = 0; i < handJSON.length(); i++) {
            JSONObject cardJSON = handJSON.getJSONObject(i);
            String ID = cardJSON.getString("ID");
            String origin = cardJSON.getString("origin");
            Card card = new Card(ID, origin);
            hand.add(card);
        }
        JSONArray possibleactionsJSON = playerstateJSON.getJSONArray("possibleactions");
        ArrayList<String> possibleactions = new ArrayList<>();
        for (int i = 0; i < possibleactionsJSON.length(); i++) {
            String action = possibleactionsJSON.getString(i);
            possibleactions.add(action);
        }
        PlayerState playerstate = new PlayerState(hand, possibleactions);

        //make messageBody
        EinzMessageBody emb = new EinzSendStateMessageBody(globalstate, playerstate);
        //make message
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;

    }

    private EinzMessage parseGetState(JSONObject message) throws JSONException {
        //make messageHeader
        EinzMessageHeader emh = new EinzMessageHeader("stateinfo", "GetState");
        //make messageBody
        EinzMessageBody emb = new EinzGetStateMessageBody();
        //make message
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

}
