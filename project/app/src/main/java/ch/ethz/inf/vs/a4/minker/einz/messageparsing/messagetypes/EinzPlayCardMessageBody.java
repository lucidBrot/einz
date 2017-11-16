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
          "color":"green",
          "num":"1337"
        }
      }
    }
     */

    private Card card;

    public Card getCard() {
        return card;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonbody = new JSONObject();
        jsonbody.put("card", this.getCard());
        return jsonbody;
    }
}
