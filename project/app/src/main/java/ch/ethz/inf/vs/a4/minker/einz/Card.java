package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fabian on 11.11.2017.
 */

public class Card {

    //text determines what kind of card you play. This can be "zero", "one", "plusTwo", ...
    private final CardText text;
    //color determines the color of the card. If the card has no color, it is "none"
    private final CardColor color;

    private final String ID;

    private final String name;

    public String origin = CardOrigin.UNSPECIFIED.value; // origin can be any of CardOrigin or a username


    public Card (String ID, String name, CardText text, CardColor color){
        this.ID = ID;
        this.name = name;
        this.text = text;
        this.color = color;
    }

    /**
     *
     * @param ID
     * @param name
     * @param text
     * @param color
     * @param origin origin can be any of CardOrigin or a username
     */
    public Card (String ID, String name, CardText text, CardColor color, String origin){
        this(ID, name, text, color);
        this.origin = origin;
    }
    /**
    * param origin origin can be any of CardOrigin or a username
     */
    public Card (String ID, String origin){
        Card card = new CardLoader().getCardInstance(ID);
        this.ID=card.ID;
        this.name=card.name;
        this.text=card.text;
        this.color=card.color;
        this.origin=origin;
    }

    public String getOrigin() {
        return origin;
    }


    public CardText getText() {
        return text;
    }

    /**
     * Name that can be displayed to the user in the UI
     * @return The name of the Card
     */
    public String getName() {
        return name;
    }
    public CardColor getColor() {
        return color;
    }

    public String getID() {
        return ID;
    }

    public JSONObject toJSON(){
        JSONObject card = new JSONObject();
        try {
            card.put("ID", this.ID);
            card.put("origin", this.origin);
        } catch (JSONException e) {
            e.printStackTrace(); // this will not happen. EVER.
        }
        return card;
    }

}
