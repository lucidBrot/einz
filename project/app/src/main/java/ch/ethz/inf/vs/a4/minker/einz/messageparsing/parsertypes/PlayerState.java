package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.Card;

/**
 * Created by silvia on 11/17/17.
 */
//this class is used for EinzSendStateMessageBody
public class PlayerState {
    private ArrayList<Card> hand;
    private ArrayList<String> possibleActions;

    public PlayerState(ArrayList<Card> hand, ArrayList<String> possibleActions){
        this.hand = hand;
        this.possibleActions = possibleActions;
    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    public ArrayList<String> getPossibleActions() {
        return possibleActions;
    }

    public void setPossibleActions(ArrayList<String> possibleactions) {
        this.possibleActions = possibleactions;
    }

    public JSONObject toJSON() throws JSONException {
        // build hand
        JSONArray handJSON = new JSONArray();
        ArrayList<Card> hand = getHand();
        for (int i = 0; i<hand.size(); i++){
            Card card = hand.get(i);
            JSONObject cardJSON = new JSONObject();
            cardJSON.put(card.ID, card.origin);
            handJSON.put(i, cardJSON);
        }
        // build possibleactions
        JSONArray possibleactionsJSON = new JSONArray();
        ArrayList<String> possibleactions = getPossibleActions();
        for (int i = 0; i<possibleactions.size(); i++){
            String action = possibleactions.get(i);
            possibleactionsJSON.put(i, action);
        }
        // put it all together
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hand", handJSON);
        jsonObject.put("possibleactions", possibleactionsJSON);
        return jsonObject;
    }
}