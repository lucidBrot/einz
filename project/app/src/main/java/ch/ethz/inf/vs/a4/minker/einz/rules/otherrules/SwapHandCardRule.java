package ch.ethz.inf.vs.a4.minker.einz.rules.otherrules;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.SelectorRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Josua on 12/16/17.
 */

public class SwapHandCardRule extends BasicCardRule implements SelectorRule {

    @Override
    public String getName() {
        return "Swap Cards";
    }

    @Override
    public String getDescription() {
        return "When the assigned card is played you can choose a different Player and swap your hand" +
                "cards with that player.";
    }

    @Override
    public Map<String, String> getChoices(GlobalState state) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("Don't swap", "Don't swap");
        HashMap<String, Integer> players = state.getPlayerHandSizeOrdered();
        for(String player : players.keySet()){
            if(!player.equals(state.getActivePlayer().getName())){
                result.put(player, player + " (" + players.get(player) + " Cards)");
            }
        }
        return result;
    }

    @Override
    public String getSelectionTitle() {
        return "Select a player to swap your hand with";
    }

    @Override
    public JSONObject makeSelectionReadyForSend(String selection) throws JSONException{
        JSONObject result = new JSONObject();
        result.put("swapPartner", selection);
        return result;
    }

    @Override
    public GlobalState onPlayAssignedCardChoice(GlobalState state, JSONObject rulePlayParams) {
        String swapPartner = "";
        try{
            swapPartner = rulePlayParams.getString("swapPartner");
        } catch (JSONException e){
            e.printStackTrace();
        }
        Player swapPlayer = state.getPlayer(swapPartner);
        if(swapPlayer != null){
            List<Card> partnerHand = swapPlayer.hand;
            swapPlayer.hand = state.getActivePlayer().hand;
            state.getActivePlayer().hand = partnerHand;
        }
        return state;
    }


}
