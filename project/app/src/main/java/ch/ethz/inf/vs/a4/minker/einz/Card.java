package ch.ethz.inf.vs.a4.minker.einz;

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

    public String origin;


    public Card (String ID, String name, CardText text, CardColor color){
        this.ID = ID;
        this.text = text;
        this.color = color;
        this.name = name;
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

    public JSONObject toJSON(){ // TODO: implement toJSON
        return null;
    }

}
