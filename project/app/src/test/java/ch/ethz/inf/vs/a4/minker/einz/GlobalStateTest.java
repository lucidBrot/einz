package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;

import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Josua on 12/8/17.
 */

public class GlobalStateTest {

    private JSONObject generateSerializedGlobalState(String activePlayer,
                                                     int cardsToDraw,
                                                     List<String> players,
                                                     List<Integer> handSize,
                                                     List<String[]> cards){
        try {
            JSONArray hand = new JSONArray();
            for (int i = 0; i < players.size(); i++) {
                JSONObject player = new JSONObject();
                player.put("name", players.get(i));
                player.put("handSize",  handSize.get(i));
                hand.put(player);
            }

            JSONArray stack = new JSONArray();
            for (String[] card : cards) {
                JSONObject cardObj = new JSONObject();
                cardObj.put("ID", card[0]);
                cardObj.put("origin", card[1]);
                stack.put(cardObj);
            }

            JSONObject serializedState = new JSONObject();
            serializedState.put("activePlayer", activePlayer);
            serializedState.put("cardsToDraw", cardsToDraw);
            serializedState.put("numCardsInHand", hand);
            serializedState.put("stack", stack);

            JSONObject lastRuleSelection = new JSONObject();
            lastRuleSelection.put("rule","data");

            serializedState.put("lastRuleSelection", lastRuleSelection);

            return serializedState;

        } catch (Exception e){
            throw new RuntimeException("Failed initializing");
        }
    }

    private void loadTestCards(){
        String JSONString = "[\n" +
                "  {\n" +
                "    \"ID\":\"yellow_2\", \"name\":\"Yellow 0\", \"text\":\"ZERO\", \"color\":\"YELLOW\", \"resourceGroup\":\"drawable\", \"resourceName\":\"card_0_yellow\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_3\", \"name\":\"Yellow 1\", \"text\":\"ONE\", \"color\":\"YELLOW\", \"resourceGroup\":\"drawable\", \"resourceName\":\"card_1_yellow\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"blue_1\", \"name\":\"Yellow 2\", \"text\":\"TWO\", \"color\":\"YELLOW\",\"resourceGroup\":\"drawable\", \"resourceName\":\"card_2_yellow\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"asdf\", \"name\":\"Yellow 2\", \"text\":\"TWO\", \"color\":\"YELLOW\",\"resourceGroup\":\"drawable\", \"resourceName\":\"card_2_yellow\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"hoi\", \"name\":\"Yellow 2\", \"text\":\"TWO\", \"color\":\"YELLOW\",\"resourceGroup\":\"drawable\", \"resourceName\":\"card_2_yellow\",\n" +
                "  },\n" +
                "]";

        try {
            EinzSingleton.getInstance().getCardLoader().loadCards( new JSONArray(JSONString));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testDeserialize(){
        int cardsToDraw = 5;
        String activePlayer = "Fritz";
        ArrayList<String> players = new ArrayList<>();
        ArrayList<Integer> handSize = new ArrayList<>();
        players.add("Hans");
        handSize.add(5);
        players.add(activePlayer);
        handSize.add(3);
        players.add("Alfred");
        handSize.add(20);

        ArrayList<String[]> cards = new ArrayList<>();
        cards.add(new String[] {"yellow_2", "Fritz"});
        cards.add(new String[] {"yellow_3", "Hans"});
        cards.add(new String[] {"blue_1", "Kunz"});
        cards.add(new String[] {"asdf", "hoi"});
        cards.add(new String[] {"hoi", "temmmie"});

        loadTestCards();


        JSONObject serializedState =  generateSerializedGlobalState(activePlayer,cardsToDraw,players,handSize,cards);
        GlobalState state;
        try {
            state = GlobalState.fromJSON(serializedState);

            assertEquals("cardsToDraw does not match", cardsToDraw, state.getCardsToDraw());
            assertEquals("activePlayer name does not match", activePlayer, state.getActivePlayer().getName());
            assertEquals("Card ID does not match", cards.get(cards.size() - 1)[0], state.getTopCardDiscardPile().getID());
            assertEquals("Card origin does not match", cards.get(cards.size() - 1)[1], state.getTopCardDiscardPile().getOrigin());

            LinkedHashMap<String, Integer> order = state.getPlayerHandSizeOrdered();
            assertEquals("not same amount of players", players.size(), order.size());
            Iterator<String> iterator  = order.keySet().iterator();
            for (int i = 0; i < order.size(); i++) {
                String player = iterator.next();
                assertEquals("Order of playersdoes not match at index " + i, players.get(i), player);
                assertEquals("Hand-size does not match", handSize.get(i), order.get(player));
            }

            List<Card> actualStack = state.getDiscardPile();
            assertEquals("not same amount of players", cards.size(), actualStack.size());
            for (int i = 0; i < cards.size(); i++) {
                assertEquals("Order of stack does not match at index " + i, cards.get(i)[0], actualStack.get(i).getID());
                assertEquals("Not same origin", cards.get(i)[1], actualStack.get(i).getOrigin());
            }
        } catch (Exception e){
            e.printStackTrace();
            fail("Failed to get the GlobalState. see exception");
        }
    }
}
