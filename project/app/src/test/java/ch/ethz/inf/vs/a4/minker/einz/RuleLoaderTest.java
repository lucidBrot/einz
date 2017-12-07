package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.rules.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.RuleLoader;
import org.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Josua on 11/28/17.
 */



public class RuleLoaderTest {

    @Test
    public void testLoad(){
        RuleLoader loader = new RuleLoader();
        JSONArray arr = new JSONArray();
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.PlayColorRule");
        try{
            loader.loadRules(arr);
        } catch (Exception e){

        }
        System.out.println(loader.getRulesNames());
        BasicRule loadedRule = loader.getInstanceOfRule("Play Color on Color");
        assertTrue("Not an instance of the right class", loadedRule instanceof ch.ethz.inf.vs.a4.minker.einz.rules.PlayColorRule);

    }

    @Test
    public void testMultiple(){
        RuleLoader loader = new RuleLoader();
        JSONArray arr = new JSONArray();
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.ChangeDirectionRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.DrawTwoCardsRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.GameEndsOnWinRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.PlayAlwaysRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.PlayColorRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.PlayTextRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.StartGameWithCardsRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.WinOnNoCardsRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.WishColorRule");
        try {
            loader.loadRules(arr);
        } catch (Exception e){

        }
        assertTrue(loader.getRulesNames().size() == 9);
        for(String name : loader.getRulesNames()){
            BasicRule loadedRule = loader.getInstanceOfRule(name);
            assertEquals("Not an instance of the right class", loadedRule.getName(), name);
        }
    }
}
