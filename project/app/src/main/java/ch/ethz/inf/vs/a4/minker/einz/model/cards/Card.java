package ch.ethz.inf.vs.a4.minker.einz.model.cards;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Created by Fabian on 11.11.2017.
 */

public class Card {

    //text determines what kind of card you play. This can be "zero", "one", "plusTwo", ...
    private final CardText text;
    //color determines the color of the card. If the card has no color, it is "none"
    private final CardColor color;

    private final String ID; // unique identifier that maps to name, resourceName and resourceGroup

    private final String name; // name that can be displayed to the user in the UI

    private final String resourceName;

    private final String resourceGroup;

    private String origin = CardOrigin.UNSPECIFIED.value; // origin can be any of CardOrigin or a username. // Could be final. would that make sense?

    /**
     * <b>Are you sure you shouldn't be using {@link ch.ethz.inf.vs.a4.minker.einz.CardLoader#getCardInstance(String, String)} instead?</b><br>
     *
     * @param ID             see 'specs' in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param name           can be any String. This should probably be specified somewhere. Currently, all I know is that it has the format <code>"Yellow 1"</code>
     *                       in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param text           can be any of {@link CardText}
     * @param color          can be any of {@link CardColor}
     * @param origin         origin can be any of {@link CardOrigin} or a username
     */
    public Card(String ID, String name, CardText text, CardColor color, String resourceGroup, String resourceName, String origin) {
        this.ID = ID;
        this.name = name;
        this.text = text;
        this.color = color;
        this.resourceGroup = resourceGroup;
        this.resourceName = resourceName;
        this.origin = origin;
    }

    /**
     * <b>"Deprecated" because you should use CardLoader in most cases!</b><br>
     * Calls {@link #Card(String, String, CardText, CardColor, String, String, String)}
     * with <code>playParameters = null</code> and <code>origin = CardOrigin.UNSPECIFIED.value</code>
     *
     * @param ID    see 'specs' in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param name  can be any String. This should probably be specified somewhere. Currently, all I know is that it has the format <code>"Yellow 1"</code>
     *              in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param text  can be any of {@link CardText}
     * @param color can be any of {@link CardColor}
     */
    @Deprecated
    public Card(String ID, String name, CardText text, CardColor color, String resourceGroup, String resourceName) {
        this(ID, name, text, color, resourceGroup, resourceName, CardOrigin.UNSPECIFIED.value);
    }


    public String getOrigin() {
        return origin;
    }


    public CardText getText() {
        return text;
    }

    /**
     * Name that can be displayed to the user in the UI
     *
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

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public int getImageRessourceID(Context context) {
        return context.getResources().getIdentifier(resourceName, resourceGroup, context.getPackageName());
    }

    @Override
    public String toString() {

        return "Card(" + ID + ", " + color + ", " + text + ")";
    }

    public JSONObject toJSON() {
        JSONObject card = new JSONObject();
        try {
            card.put("ID", this.ID);
            if(this.origin==null){
                this.origin=CardOrigin.UNSPECIFIED.value;
            }
            card.put("origin", this.origin);
        } catch (JSONException e) {
            e.printStackTrace(); // this will not happen. EVER.
        }
        return card;
    }


    //below was probably a bad idea. what happens with that if two different cards are compared (but they only differ in origin)
//    /**
//     * {@inheritDoc}
//     *
//     * Does only compare the fields which will probably not change. I.e. a card with different origin or different parameters is still the same card
//     * @param obj
//     * @return
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if (!(obj instanceof Card)) {
//            return super.equals(obj);
//        }
//
//        Card object = (Card) obj;
//        return (
//                object.ID.equals(this.ID) &&
//                object.name.equals(this.name) &&
//                object.color.equals(this.color) &&
//                object.text.equals(this.color) &&
//                object.resourceGroup.equals(this.resourceGroup) &&
//                object.resourceName.equals(this.resourceName)
//        );
//    }
//
//    public boolean equalsExactly(Card card){
//        return this.equals(card) && this.playParameters.equals(card.playParameters) && this.origin.equals(card.origin);
//    }
//
//    @Override
//    public int hashCode() { // see https://stackoverflow.com/a/2265637/2550406
//        // two cards are equal if all their fixed things are equal
//        return Objects.hash(this.ID, this.name, this.color, this.text, this.resourceGroup, this.resourceName);
//    }
}
