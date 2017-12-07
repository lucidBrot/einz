package ch.ethz.inf.vs.a4.minker.einz;


import ch.ethz.inf.vs.a4.minker.einz.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.cards.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.cards.CardText;
import org.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josua on 11/28/17.
 */

public class CardLoaderTest {


    @Test
    public void testSingleLoad(){

        String JSONString = "[{\"ID\":\"yellow_3\",\"name\":\"Yellow 3\",\"text\":\"THREE\"," +
                "\"color\":\"YELLOW\",\"image\":\"\",}]";

        CardLoader loader = new CardLoader();
        try {
            JSONArray array = new JSONArray(JSONString);
            loader.loadCards(array);
        } catch (Exception e){
            e.printStackTrace();
        }
        Card testedCard = loader.getCardInstance("yellow_3");
        assertEquals(testedCard.getID(), "yellow_3");
        assertEquals(testedCard.getText(), CardText.THREE);
        assertEquals(testedCard.getColor(), CardColor.YELLOW);
    }

    @Test
    public void testMultiLoad(){
        String JSONString = "[\n" +
                "  {\n" +
                "    \"ID\":\"yellow_0\", \"name\":\"Yellow 0\", \"text\":\"ZERO\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_1\", \"name\":\"Yellow 1\", \"text\":\"ONE\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_2\", \"name\":\"Yellow 2\", \"text\":\"TWO\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_3\", \"name\":\"Yellow 3\", \"text\":\"THREE\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_4\", \"name\":\"Yellow 4\", \"text\":\"FOUR\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_5\", \"name\":\"Yellow 5\", \"text\":\"FIVE\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_6\", \"name\":\"Yellow 6\", \"text\":\"SIX\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_7\", \"name\":\"Yellow 7\", \"text\":\"SEVEN\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_8\", \"name\":\"Yellow 8\", \"text\":\"EIGHT\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "  {\n" +
                "    \"ID\":\"yellow_9\", \"name\":\"Yellow 9\", \"text\":\"NINE\", \"color\":\"YELLOW\", \"image\":\"\",\n" +
                "  },\n" +
                "]";
        CardLoader loader = new CardLoader();
        try {
            JSONArray array = new JSONArray(JSONString);
            loader.loadCards(array);
        } catch (Exception e){
            e.printStackTrace();
        }
        Card testedCard = loader.getCardInstance("yellow_1");
        assertEquals(testedCard.getID(), "yellow_1");
        assertEquals(testedCard.getText(), CardText.ONE);
        assertEquals(testedCard.getColor(), CardColor.YELLOW);
        Card tested2Card = loader.getCardInstance("yellow_3");
        assertEquals(tested2Card.getID(), "yellow_3");
        assertEquals(tested2Card.getText(), CardText.THREE);
        assertEquals(tested2Card.getColor(), CardColor.YELLOW);
        Card tested3Card = loader.getCardInstance("yellow_9");
        assertEquals(tested3Card.getID(), "yellow_9");
        assertEquals(tested3Card.getText(), CardText.NINE);
        assertEquals(tested3Card.getColor(), CardColor.YELLOW);
        Card tested3CardAgain = loader.getCardInstance("yellow_9");
        assertFalse(tested3Card == tested3CardAgain);
        assertTrue(tested3Card.getColor() == tested3CardAgain.getColor());
        assertTrue(tested3Card.getText() == tested3CardAgain.getText());

    }
}
