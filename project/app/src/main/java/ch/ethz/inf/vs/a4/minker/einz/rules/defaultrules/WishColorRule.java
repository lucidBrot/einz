package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.model.SelectorRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import org.json.JSONObject;

/**
 * Created by Josua on 11/27/17.
 */

public class WishColorRule extends BasicCardRule implements SelectorRule {

    private CardColor wishedColor = null;

    /**
     * Determines if a card has been wished
     */
    private boolean wished = false;

    @Override
    public String getName() {
        return "Wish Color";
    }

    @Override
    public String getDescription() {
        return "Forces the next card to be of the wished color";
    }

    @Override

    public boolean isValidPlayCardPermissive(GlobalState state, Card played) {

        Log.d("WishColorRule#" + this.toString(), "Wished color was: " + wishedColor + ", played color: " + played.getColor());

        return wished && (played.getColor().equals(wishedColor));
    }

    @Override
    public GlobalState onPlayAssignedCard(GlobalState state, Card played) {
        wished = true;
        return state;
    }

    @Override
    public GlobalState onPlayAnyCard(GlobalState state, Card played) {
        if(!assignedTo.equals(played)){
            wished = false;
        }
        return state;
    }

    @Override
    public List<String> getChoices(GlobalState state) {
        List<String> result = new ArrayList<>();
        for(CardColor color : CardColor.values()){
            result.add(color.name());
        }
        return result;
    }

    @Override
    public GlobalState onPlayAssignedCardChoice(GlobalState state, JSONObject rulePlayParams){
        wished = true;
        String choice = "";
        try{
            choice = rulePlayParams.getString("wishForColor").toUpperCase();
            wishedColor = CardColor.valueOf(choice);
        } catch (Exception e){
            wishedColor = null;
            Log.w("wishColorRule", "Color wished for was bad: "+choice);
        }
        Log.d("wishColorRule#"+this.toString(),"Choice was " + choice);
        Log.d("wishColorRule#"+this.toString(),"Made choice " + wishedColor);
        return state;
    }
}
