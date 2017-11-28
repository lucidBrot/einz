package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Josua on 11/28/17.
 */

public class CardLoader {
    private Map<String, CardAttributeContainer> cardMapping;


    public CardLoader(){
        cardMapping = new HashMap<>();
    }

    public Card getCardInstance(String cardID){
        if(!cardMapping.containsKey(cardID)){
            return null;
        }
        CardAttributeContainer params = cardMapping.get(cardID);
        return new Card(cardID, params.text, params.color);
    }


    public void loadCards(JSONArray cardDefinitions) throws JSONException {

        for(int i = 0; i < cardDefinitions.length(); i++){
            try {
                JSONObject cardObject = cardDefinitions.getJSONObject(i);

                String ID = cardObject.getString("ID");
                String name = cardObject.getString("name");
                String text = cardObject.getString("text");
                String color = cardObject.getString("color");
                String image = cardObject.getString("image");

                CardText cardText = CardText.valueOf(text);
                CardColor cardColor = CardColor.valueOf(color);

                CardAttributeContainer params = new CardAttributeContainer(name, cardText, cardColor, image);
                cardMapping.put(ID, params);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
        }
    }

    private class CardAttributeContainer{
        public String name;
        public CardText text;
        public CardColor color;
        public String image;

        public CardAttributeContainer(String name, CardText text, CardColor color, String image){
            this.name = name;
            this.text = text;
            this.color = color;
            this.image = image;
        }
    }


}
