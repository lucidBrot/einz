package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzDrawCardsSuccessMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzDrawParser extends EinzParser {

    /**
     * @param message JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessage Object containing all the information specific to this kind of message
     */
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        JSONObject header = message.getJSONObject("header");
        switch(header.getString("messagetype")){
            case "DrawCardsSuccess":
                return parseDrawCardsSuccess(message);
            case "DrawCardsFailure":
                return parseDrawCardsFailure(message);
            case "DrawCards":
                return parseDrawCards(message);
            default:
                Log.d("EinzDrawParser","Not a valid messagetype "+header.getString("messagetype")+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage<EinzDrawCardsFailureMessageBody> parseDrawCardsFailure(JSONObject message) throws JSONException {
        EinzMessageHeader header = new EinzMessageHeader("draw", "DrawCardsFailure");
        JSONObject jbody = message.getJSONObject("body");
        String reason = jbody.getString("reason");
        EinzDrawCardsFailureMessageBody body = new EinzDrawCardsFailureMessageBody(reason);
        return new EinzMessage<>(header, body);
    }

    private EinzMessage parseDrawCardsSuccess(JSONObject message) throws JSONException {
        EinzMessageHeader header = new EinzMessageHeader("draw", "DrawCardsSuccess");
        JSONObject jbody = message.getJSONObject("body");
        JSONArray jcards = jbody.getJSONArray("cards");
        ArrayList<Card> cards = new ArrayList<>();
        for(int i=0; i< jcards.length();i++){
            JSONObject myCard = jcards.getJSONObject(i);
            cards.add(new Card(myCard.getString("ID"), myCard.getString("origin"))); // TODO: should parsing be done here or in "Card.java"?
        }
        return new EinzMessage<>(header, new EinzDrawCardsSuccessMessageBody(cards));
    }

    private EinzMessage parseDrawCards(JSONObject message) throws JSONException {
        return new EinzMessage<EinzDrawCardsMessageBody>(
                new EinzMessageHeader("draw", "DrawCards"),
                new EinzDrawCardsMessageBody()
        );
    }
}
