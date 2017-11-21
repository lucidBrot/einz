package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONObject;

/**
 * Created by Fabian on 11.11.2017.
 */

public class Card {

    public Card (CardTypes type, CardColors color){
        //This is used for cards which don't need the "wish" to function properly

        this.type = type;
        this.color = color;
        this.wish = CardColors.NONE;
        //TODO: Set ID according to Josua
    }
    public Card (String ID, String origin){
        this.ID = ID;
        this.origin = origin;
    }

    public Card (String ID){
        this.ID = ID;
    }

    //type determines what kind of card you play. This can be "zero", "one", "plusTwo", ...
    public CardTypes type;
    //color determines the color of the card. If the card has no color, it is "none"
    public CardColors color;
    //when playing certain cards, you can choose something (e.g. a color)
    //with all other cards, this field is ignored
    public CardColors wish;
    public String ID;
    public String origin;

    public JSONObject toJSON(){ // TODO: implement toJSON
        return null;
    }

}
