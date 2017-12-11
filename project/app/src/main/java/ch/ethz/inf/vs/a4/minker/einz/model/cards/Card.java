package ch.ethz.inf.vs.a4.minker.einz.model.cards;

import android.content.Context;

import org.json.JSONArray;
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

    private final String resourceName;

    private final String resourceGroup;

    public String origin = CardOrigin.UNSPECIFIED.value; // origin can be any of CardOrigin or a username. // Could be final. would that make sense?

    private final JSONObject playParameters; // parameters that rules may ask for, but can also be unset. E.g. what color a wish card wishes for. Is allowed to be null


    /**
     * @param ID             see 'specs' in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param name           can be any String. This should probably be specified somewhere. Currently, all I know is that it has the format <code>"Yellow 1"</code>
     *                       in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param text           can be any of {@link CardText}
     * @param color          can be any of {@link CardColor}
     * @param origin         origin can be any of {@link CardOrigin} or a username
     * @param playParameters allowed to be <code>null</code>, otherwise a valid JSONObject, see messages.md
     */
    public Card(String ID, String name, CardText text, CardColor color, String resourceGroup, String resourceName, String origin, JSONObject playParameters) {
        this.ID = ID;
        this.name = name;
        this.text = text;
        this.color = color;
        this.resourceGroup = resourceGroup;
        this.resourceName = resourceName;
        this.origin = origin;
        this.playParameters = playParameters;
    }

    /**
     * <b>"Deprecated" because you should use CardLoader in most cases!</b><br>
     * Calls {@link #Card(String, String, CardText, CardColor, String, String, String, JSONObject)}
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
        this(ID, name, text, color, resourceGroup, resourceName, CardOrigin.UNSPECIFIED.value, null);
    }

    /**
     * Calls {@link #Card(String, String, CardText, CardColor, String, String, String, JSONObject)} with <code>playParameters = null</code>
     *
     * @param ID     see 'specs' in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param name   can be any String. This should probably be specified somewhere. Currently, all I know is that it has the format <code>"Yellow 1"</code>
     *               in {@link ch.ethz.inf.vs.a4.minker.einz.R.raw#card_definition}
     * @param text   can be any of {@link CardText}
     * @param color  can be any of {@link CardColor}
     * @param origin origin can be any of {@link CardOrigin} or a username
     */
    public Card(String ID, String name, CardText text, CardColor color, String resourceGroup, String resourceName, String origin) {
        this(ID, name, text, color, resourceGroup, resourceName, origin, null);
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
            card.put("origin", this.origin);
            card.put("playParameters", this.playParameters);
        } catch (JSONException e) {
            e.printStackTrace(); // this will not happen. EVER.
        }
        return card;
    }

    /**
     * playParameters is a list of JSONObjects  which represent settings specific to this card ID when played. Exampli gratuita, a player might play a card that allows them to wish for a color. It is easiest when that selection is sent with the playCard Request.
     * <p>
     * This field will usually be ignored, unless a rule uses it. To use it, you can call yourCard.getPlayParameters("wishForColors") to get the String associated with "wishForColors" or yourCard.getPlayParameters() to get the whole JSONObject list.
     *
     * @return null if there were no params set
     */
    public JSONObject getPlayParameters() {
        return this.playParameters;
    }

    /**
     * @param paramKey identifies usually the rule to which the contained Object belongs
     * @return null if the given param JSONObject was not found, otherwise that object
     */
    public JSONObject getPlayParameters(String paramKey) {
        if (this.getPlayParameters() != null) {
            try {
                return getPlayParameters().getJSONObject(paramKey);
            } catch (JSONException e) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * @param paramKey  identifies usually the rule to which the contained Object belongs
     * @param paramName identifies the parameter within that Object. see messages.md for more explanation
     * @return null if the given param was not found, otherwise that parameter String
     */
    public String getPlayParameter(String paramKey, String paramName) {
        JSONObject obj = getPlayParameters(paramKey);
        if (obj == null) {
            return null;
        }
        try {
            return obj.getString(paramName);
        } catch (JSONException e) {
            return null;
        }
    }


}
