package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
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
    },
     "playParameters":{
    	"wishColorRule":{"wishForColor":"blue"},
    	"ruleDank":{"xXx":"1337"}
    }

  }
}
     */

    private final Card card;

    /**
     * Initializes the PlayCardMessageBody with the card to play and no additional parameters
     */
    public EinzPlayCardMessageBody(Card card){
        this(card, new JSONObject());
    }

    /**
     * Initializes the PlayCardMessageBody with the card to play and additional parameters that should be of the form <br><code>
     *     {<br>
     *          "wishColorRule":{"wishForColor":"blue"},<br>
     *              "ruleDank":{"xXx":"1337"}<br>
     *      }
     * </code>
     */
    public EinzPlayCardMessageBody(Card card, JSONObject playParameters){
        this.card = card;
        this.playParameters = playParameters==null?new JSONObject():playParameters;
    }

    public Card getCard() {
        return card;
    }

    private final JSONObject playParameters;

    public JSONObject getPlayParameters() {
        return playParameters;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonbody = new JSONObject();
        Card card = getCard();
        JSONObject IDJSON = new JSONObject();
        IDJSON.put("ID", card.getID());
        jsonbody.put("card", IDJSON);
        jsonbody.put("playParameters", playParameters);
        return jsonbody;
    }
}
