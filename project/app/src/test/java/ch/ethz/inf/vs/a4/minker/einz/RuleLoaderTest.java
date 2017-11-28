package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONArray;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Josua on 11/28/17.
 */



public class RuleLoaderTest {

    @Test
    public void testLoad(){
        RulesLoader loader = new RulesLoader();
        JSONArray arr = new JSONArray();
        arr.put("ch.ethz.inf.vs.a4.minker.einz.rules.PlayColorRule");
        try {
            loader.loadRules(arr);
        } catch (Exception e){

        }
        System.out.println(loader.getRulesNames());
        BasicRule loadedRule = loader.getInstanceOfRule("Play Color on Color");
        assertTrue("Not an instance of the right class", loadedRule instanceof ch.ethz.inf.vs.a4.minker.einz.rules.PlayColorRule);

    }
}
