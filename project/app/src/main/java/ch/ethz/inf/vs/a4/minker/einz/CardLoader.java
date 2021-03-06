package ch.ethz.inf.vs.a4.minker.einz;

import android.content.Context;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.InvalidResourceFormatException;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardOrigin;
import com.google.common.io.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;

import javax.annotation.Nullable;

/**
 * Created by Josua on 11/28/17.
 */

public class CardLoader {
    private Map<String, CardAttributeContainer> cardMapping;


    public CardLoader() {
        cardMapping = new HashMap<>();
    }

    /**
     * @param cardID
     * @param cardOrigin
     * @return the Card with the specified ID or <code>null</code> if it was not found in our mappings
     */
    public Card getCardInstance(String cardID, String cardOrigin) {
        if (!cardMapping.containsKey(cardID)) {
            //return null;
            // TODO: remove this debug card once all cards are registered in the json resource
            Log.w("CardLoader", "unmapped card requested: "+cardID);
            return new Card("debug", "debug", CardText.DEBUG, CardColor.NONE, "drawable", "card_choose");
            // return getCardInstance("debug"); // This is bad because the mapping-not-found should not rely on a mapping
            // return new Card("blue_1", CardOrigin.STACK.value, CardText.DEBUG, CardColor.BLUE, "drawable", "card_1_blue");
            // return getCardInstance("yellow_skip");
        }
        CardAttributeContainer params = cardMapping.get(cardID);
        return new Card(cardID, params.name, params.text, params.color, params.resourceGroup, params.resourceName, cardOrigin);
    }

    /**
     * @param cardID
     * @return the Card with the specified ID or <code>null</code> if it was not found in our mappings and as origin {@link CardOrigin#UNSPECIFIED}
     */
    public Card getCardInstance(String cardID) {
        return getCardInstance(cardID, CardOrigin.UNSPECIFIED.value);
    }


    public void loadCards(JSONArray cardDefinitions) throws JSONException {

        for (int i = 0; i < cardDefinitions.length(); i++) {
            try {
                JSONObject cardObject = cardDefinitions.getJSONObject(i);

                String ID = cardObject.getString("ID");
                String name = cardObject.getString("name");
                String text = cardObject.getString("text");
                String color = cardObject.getString("color");
                String resourceGroup = cardObject.getString("resourceGroup");
                String resourceName = cardObject.getString("resourceName");

                CardText cardText = CardText.valueOf(text);
                CardColor cardColor = CardColor.valueOf(color);

                CardAttributeContainer params = new CardAttributeContainer(name, cardText, cardColor, resourceGroup, resourceName);
                cardMapping.put(ID, params);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    public Set<String> getCardIDs(){
        return cardMapping.keySet();
    }

    /**
     * @param applicationContext
     * @param resourceFile       A file containing a JSONArray of JSONObjects which represent cards in the form
     *                           {\n" +
     *                           "    \"ID\":\"yellow_0\", \"name\":\"Yellow 0\", \"text\":\"ZERO\", \"color\":\"YELLOW\", \"resourceGroup\":\"drawable\", \"resourceName\":\"card_0_yellow\",\n" +
     *                           "  }
     * @throws JSONException
     */
    public void loadCardsFromResourceFile(Context applicationContext, int resourceFile) throws JSONException {
        InputStream jsonStream = applicationContext.getResources().openRawResource(resourceFile);
        JSONArray jsonArray = new JSONArray(convertStreamToString(jsonStream));
        this.loadCards(jsonArray);
    }

    // https://stackoverflow.com/questions/6774579/typearray-in-android-how-to-store-custom-objects-in-xml-and-retrieve-them
    // utility function
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private class CardAttributeContainer {
        public String name;
        public CardText text;
        public CardColor color;
        public String resourceGroup;
        public String resourceName;

        CardAttributeContainer(String name, CardText text, CardColor color, String resourceGroup, String resourceName) {
            this.name = name;
            this.text = text;
            this.color = color;
            this.resourceGroup = resourceGroup;
            this.resourceName = resourceName;
        }
    }


}
