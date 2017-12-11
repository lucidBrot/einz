package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardResponseMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzPlayCardParser extends EinzParser {
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch (messagetype) {
            case "PlayCard":
                return parsePlayCard(message);
            case "PlayCardResponse":
                return parsePlayCardResponse(message);
            default:
                Log.d("EinzPlayCardParser", "Not a valid messagetype " + messagetype + " for EinzPlayCardParser");
                return null;
        }
    }


    private EinzMessage parsePlayCard(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("playcard", "PlayCard");
        JSONObject body = message.getJSONObject("body");

        //get card
        JSONObject cardJSON = body.getJSONObject("card");
        String ID = cardJSON.getString("ID");
        String origin = cardJSON.getString("origin");

        JSONObject playParams = cardJSON.optJSONObject("playParameters");

        Card card = EinzSingleton.getInstance().getCardLoader().getCardInstance(ID, origin, playParams);

        //put it all together
        EinzMessageBody emb = new EinzPlayCardMessageBody(card);
        EinzMessage einzMessage = new EinzMessage<>(emh, emb);
        return einzMessage;
    }

    private EinzMessage parsePlayCardResponse(JSONObject message) throws JSONException {
        EinzMessageHeader emh = new EinzMessageHeader("playcard", "PlayCardResponse");
        JSONObject body = message.getJSONObject("body");
        //get success
        String success = body.getString("success");
        //put it all together
        EinzMessageBody emb = new EinzPlayCardResponseMessageBody(success);
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

}
