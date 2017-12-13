package ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules;

import java.util.ArrayList;
import java.util.List;

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

    private boolean wished = false;

    @Override
    public String getName() {
        return "Wish color";
    }

    @Override
    public String getDescription() {
        return "Forces the next card to be of the wished color";
    }

    @Override
    public boolean isValidPlayCardRestrictive(GlobalState state, Card played) {
        return !wished || played.getColor().equals(wishedColor);
    }

    @Override
    public GlobalState onPlayAssignedCard(GlobalState state, Card played) {
        wished = true;
        /*ArrayList<String> options = new ArrayList<>();
        for(CardColor color : CardColor.values()){
            options.add(color.color);
        }

        String result = config.getClien tCallbackService().getSelectionFromPlayer(state.getActivePlayer(), options);
        wishedColor = CardColor.valueOf(result);*/
        JSONObject params = state.getPlayParameter("wishColorRule");
        if(params!=null && !params.equals(new JSONObject())){
            wishedColor = CardColor.valueOf(params.optString("wishedColor"));
        } else {
            // TODO: what to do if no color sent?
            wishedColor = CardColor.NONE;
        }
        // Idee: wenn die Karte gespielt wird, muss die UI sowieso wissed dass der user eine farbe auswählen muss. Also user direkt farbe auswählen lassen.
        //      Danach die karte clientside mit diesem parameter setzen.
        //      Wenn server die karte erhält wird diese regel getriggert und die liest den parameter aus.

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
}
