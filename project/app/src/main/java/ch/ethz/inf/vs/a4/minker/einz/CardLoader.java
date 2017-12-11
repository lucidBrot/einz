package ch.ethz.inf.vs.a4.minker.einz;

import android.content.Context;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.InvalidResourceFormatException;
import com.google.common.io.Resources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;

/**
 * Created by Josua on 11/28/17.
 */

public class CardLoader {
    private Map<String, CardAttributeContainer> cardMapping;


    public CardLoader(){
        cardMapping = new HashMap<>();
    }

    /**
     * @param cardID
     * @return the Card with the specified ID or <code>null</code> if it was not found in our mappings
     */
    public Card getCardInstance(String cardID) {
        if(!cardMapping.containsKey(cardID)){
            return null;
        }
        CardAttributeContainer params = cardMapping.get(cardID);
        return new Card(cardID, params.name, params.text, params.color, params.resourceGroup, params.resourceName);
    }


    public void loadCards(JSONArray cardDefinitions) throws JSONException {

        for(int i = 0; i < cardDefinitions.length(); i++){
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
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }

    public void loadCardsFromResourceFile(Context applicationContext, int resourceFile) throws JSONException{
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

    private class CardAttributeContainer{
        public String name;
        public CardText text;
        public CardColor color;
        public String resourceGroup;
        public String resourceName;

        CardAttributeContainer(String name, CardText text, CardColor color, String resourceGroup, String resourceName){
            this.name = name;
            this.text = text;
            this.color = color;
            this.resourceGroup = resourceGroup;
            this.resourceName = resourceName;
        }
    }


}
