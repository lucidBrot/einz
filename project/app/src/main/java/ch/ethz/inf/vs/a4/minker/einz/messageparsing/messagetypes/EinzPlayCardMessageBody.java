package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzPlayCardMessageBody extends EinzMessageBody{

    /*
    {
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlayCard"
  },
  "body":{
    "card":{
      "ID":"cardID1337"
    }
  }
}
     */

    private final Card card;

    public EinzPlayCardMessageBody(Card card){
        this.card = card;
    }

    public Card getCard() {
        return card;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonbody = new JSONObject();
        Card card = getCard();
        JSONObject IDJSON = new JSONObject();
        IDJSON.put("ID", card.ID);
        jsonbody.put("card", IDJSON);
        return jsonbody;
    }
}
