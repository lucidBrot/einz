package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by silvia on 11/17/17.
 */
//this class is used for EinzSendStateMessageBody
public class PlayerState {
    private ArrayList<Card> hand;
    private ArrayList<String> possibleActionsNames; // Contains the names of the possible actions
    private ArrayList<JSONObject> possibleActions; // Contains the whole possible actions, including parameters

    public PlayerState(ArrayList<Card> hand, ArrayList<JSONObject> possibleActionsJSON) throws JSONException {
        this.hand = hand;
        this.possibleActionsNames = new ArrayList<>();
        this.possibleActions = new ArrayList<>();
        for(JSONObject possibleAction : possibleActionsJSON){
            if(!possibleAction.has("parameters")){
                possibleAction.put("parameters", new JSONArray()); // create empty parameters field if not existent
            } else {
                JSONObject tempParams = possibleAction.getJSONObject("parameters"); // triggers exception if not a jsonArray
                // so now we can guarantee that every object in this.possibleActions has the fields parameters and actionName
            }
            this.possibleActionsNames.add(possibleAction.getString("actionName"));
            this.possibleActions.add(possibleAction);
        }

    }

    public ArrayList<Card> getHand() {
        return hand;
    }

    /**
     * @return a list of possibleAction JSONObjects that guarantee to have a String <code>name</code> and a JSONArray <code>parameters</code>
     * <br>This method does not guarantee anything about the content of this JSONArray <code>parameters</code>
     */
    public ArrayList<JSONObject> getPossibleActions() {
        return possibleActions;
    }

    public void setHand(ArrayList<Card> hand) {
        this.hand = hand;
    }

    /**
     * @return only the names of the possibleActions, without the parameters
     */
    public ArrayList<String> getPossibleActionsNames() {
        return possibleActionsNames;
    }

    /**
     * Use this only if there are no other threads holding references to this
     * @param possibleActions will be set internally. also, {@link #possibleActionsNames} will be updated accordingly
     * @throws JSONException
     */
    public void setPossibleActions(ArrayList<JSONObject> possibleActions) throws JSONException {
        ArrayList<String> names = new ArrayList<>();
        for(JSONObject o : possibleActions){
            names.add(o.getString("actionName"));
        }
        this.possibleActionsNames = names;
        this.possibleActions = possibleActions;
    }

    public JSONObject toJSON() throws JSONException {
        // build hand
        JSONArray handJSON = new JSONArray();
        ArrayList<Card> hand = getHand();
        for (int i = 0; i<hand.size(); i++){
            Card card = hand.get(i);
            JSONObject cardJSON = new JSONObject();
            cardJSON.put("origin", card.getOrigin());
            cardJSON.put("ID", card.getID());
            handJSON.put(i, cardJSON);
        }
        // build possibleactions
        JSONArray possibleactionsJSON = new JSONArray();
        ArrayList<JSONObject> possibleactions = getPossibleActions();
        for (JSONObject o : possibleactions){
            possibleactionsJSON.put(o);
        }
        // put it all together
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("hand", handJSON);
        jsonObject.put("possibleactions", possibleactionsJSON);
        return jsonObject;
    }
}