package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.*;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

        //get global state if it is not null (could happen if error on getting state)
        GlobalStateParser globalstate = null;
        if(body.has("globalstate")) {
            JSONObject globalstateJSON = body.getJSONObject("globalstate");
            // if the globalstate object exists, then it should contain all details
            JSONArray numcardsinhandJSON = globalstateJSON.getJSONArray("numcardsinhand");
            HashMap<String, String> numcardsinhand = new HashMap<>();

            for (int i=0; i<numcardsinhandJSON.length(); i++) {
                JSONObject entry = numcardsinhandJSON.getJSONObject(i);
                String name = entry.getString("name");
                String num = entry.getString("handSize");
                numcardsinhand.put(name, num);
            }
            JSONArray stackJSON = globalstateJSON.getJSONArray("stack");
            CardLoader cardLoader = new CardLoader();
            ArrayList<Card> stack = new ArrayList<>();
            for (int i = 0; i < stackJSON.length(); i++) {
                JSONObject cardJSON = stackJSON.getJSONObject(i);
                String ID = cardJSON.getString("ID");
                String origin = cardJSON.getString("origin");

                JSONObject playParams = cardJSON.optJSONObject("playParameters");

                Card card = new Card(ID, origin, playParams);
                stack.add(card);
            }
            String whoseturn = globalstateJSON.getString("whoseturn");
            String drawxcardsmin = globalstateJSON.getString("drawxcardsmin");
            globalstate = new GlobalStateParser(numcardsinhand, stack, whoseturn, drawxcardsmin);
        }

        //get playerstate. Again, if it is nulll, there was an error. otherwise, it should have all content
        PlayerState playerstate = null;
        if(body.has("playerstate")) {
            JSONObject playerstateJSON = body.getJSONObject("playerstate");
            JSONArray handJSON = playerstateJSON.getJSONArray("hand");
            ArrayList<Card> hand = new ArrayList<>();
            for (int i = 0; i < handJSON.length(); i++) {
                JSONObject cardJSON = handJSON.getJSONObject(i);
                String ID = cardJSON.getString("ID");
                String origin = cardJSON.getString("origin");

                JSONObject playParams = cardJSON.optJSONObject("playParameters");

                Card card = new Card(ID, origin, playParams);
                // Card card = new Card(ID, origin, CardText.DEBUG, CardColor.BLUE, "drawable", "card_1_blue"); // TODO: use CardLoader that is somehow globally available
                hand.add(card);
            }
            JSONArray possibleactionsJSON = playerstateJSON.getJSONArray("possibleactions");
            ArrayList<JSONObject> possibleactions = new ArrayList<>();
            for (int i = 0; i < possibleactionsJSON.length(); i++) {
                JSONObject action = possibleactionsJSON.getJSONObject(i);
                possibleactions.add(action);
            }
            playerstate = new PlayerState(hand, possibleactions);
        }

        if(!(body.has("playerstate")&&body.has("globalstate"))){
            Log.i("StateInfoParser/onSendState", "Received state with playerstate or globalstate null. Maybe you requested a state before the game started?");
        }

        //make messageBody
        EinzMessageBody emb = new EinzSendStateMessageBody(globalstate, playerstate);
        //make message
        EinzMessage einzMessage = new EinzMessage<>(emh, emb);
        return einzMessage;

    }

    private EinzMessage parseGetState(JSONObject message) throws JSONException {
        //make messageHeader
        EinzMessageHeader emh = new EinzMessageHeader("stateinfo", "GetState");
        //make messageBody
        EinzMessageBody emb = new EinzGetStateMessageBody();
        //make message
        EinzMessage einzMessage = new EinzMessage<>(emh, emb);
        return einzMessage;
    }

}

