package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayColorRule;

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
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayColorRule");
        try{
            loader.loadRules(arr);
        } catch (Exception e){

        }
        System.out.println(loader.getRulesNames());
        BasicRule loadedRule = loader.getInstanceOfRule("Play Color on Color");
        assertTrue("Not an instance of the right class", loadedRule instanceof PlayColorRule);

    }

    @Test
    public void testMultiple(){
        RuleLoader loader = new RuleLoader();
        JSONArray arr = new JSONArray();
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.ChangeDirectionRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.DrawTwoCardsRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.GameEndsOnWinRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayAlwaysRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayColorRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayTextRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.StartGameWithCardsRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.WinOnNoCardsRule");
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.WishColorRule");
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
