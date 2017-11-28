package ch.ethz.inf.vs.a4.minker.einz;


import org.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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
}
