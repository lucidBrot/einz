package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by silvia on 11/17/17.
 */
//this class is used for EinzSendStateMessageBody
public class GlobalStateParser {
    private HashMap<String, String> numcardsinhand;
    private ArrayList<Card> stack;
    private String whoseTurn;
    private String drawxCardsMin;
    private JSONObject playParameters;

    /**
     * @param numcardsinhand
     * @param stack
     * @param whoseTurn
     * @param drawxCardsMin
     * @param playParameters see the docs
     */
    public GlobalStateParser(HashMap<String, String> numcardsinhand, ArrayList<Card> stack, String whoseTurn, String drawxCardsMin, JSONObject playParameters){
        this.numcardsinhand = numcardsinhand;
        this.stack = stack;
        this.whoseTurn = whoseTurn;
        this.drawxCardsMin = drawxCardsMin;
        this.playParameters=playParameters==null?new JSONObject():playParameters;
    }

    public GlobalStateParser(HashMap<String, String> numcardsinhand, ArrayList<Card> stack, String whoseTurn, String drawxCardsMin){
        this(numcardsinhand, stack, whoseTurn, drawxCardsMin, new JSONObject());
    }

    public String getWhoseTurn() {
        return whoseTurn;
    }

    public void setWhoseTurn(String whoseTurn) {
        this.whoseTurn = whoseTurn;
    }

    public JSONObject getPlayParameters() {
        return playParameters;
    }

    public void setPlayParameters(JSONObject playParameters) {

        this.playParameters = playParameters;
    }

    public HashMap<String, String> getNumcardsinhand() {

        return numcardsinhand;
    }

    public String getDrawxCardsMin() {
        return drawxCardsMin;
    }

    public void setDrawxCardsMin(String drawxCardsMin) {
        this.drawxCardsMin = drawxCardsMin;
    }

    public ArrayList<Card> getStack() {
        return stack;
    }

    public void setStack(ArrayList<Card> stack) {
        this.stack = stack;
    }

    public HashMap<String, String> getNumCardsInHand() {
        return numcardsinhand;
    }

    public void setNumCardsInHand(HashMap<String, String> numCardsInHand) {
        this.numcardsinhand = numCardsInHand;
    }

    public JSONObject toJSON() throws JSONException {
        //build numcardsinhand object
        JSONArray numcardsinhandJSON = new JSONArray();
        HashMap<String, String> numcardsinhand = getNumCardsInHand();
        for (Map.Entry<String, String> entry : numcardsinhand.entrySet()) {
            String name = entry.getKey();
            String number = entry.getValue();
            JSONObject player = new JSONObject();
            player.put("handSize",number);
            player.put("name", name);
            numcardsinhandJSON.put(player);
        }
        //build stack object
        JSONArray stackJSON = new JSONArray();
        ArrayList<Card> stack = getStack();
        for (int i = 0; i<stack.size(); i++){
            Card card = stack.get(i);
            JSONObject cardJSON = new JSONObject();
            cardJSON.put("origin", card.getOrigin());
            cardJSON.put("ID", card.getID());
            stackJSON.put(i, cardJSON);
        }
        //get whoseturn
        String whoseturn = getWhoseTurn();
        //get drawxcardsmin
        String drawxcardsmin = getDrawxCardsMin();

        // if playParameters not set, set to empty
        if(playParameters==null){
            playParameters = new JSONObject();
        }

        //put it all together
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("numcardsinhand", numcardsinhandJSON);
        jsonObject.put("stack", stackJSON);
        jsonObject.put("whoseturn", whoseturn);
        jsonObject.put("drawxcardsmin", drawxcardsmin);
        jsonObject.put("playParameters", playParameters);

        return jsonObject;
    }

}