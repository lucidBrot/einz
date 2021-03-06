package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzDrawCardsSuccessMessageBody extends EinzMessageBody {

    private final ArrayList<Card> cards;

    public EinzDrawCardsSuccessMessageBody(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONArray jcards = new JSONArray();
        for(Card card : cards){
            jcards.put(card.toJSON());
        }
        JSONObject body =  new JSONObject();
        body.put("cards", jcards);
        return body;
    }
}
