package ch.ethz.inf.vs.a4.minker.einz.rules;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.inf.vs.a4.minker.einz.BasicRule;

/**
 * Created by Josua on 11/22/17.
 */

public class RulesLoader {

    public List<BasicRule> loadRules(JSONObject loaded){
        //TODO: Read from JSON
        ArrayList<BasicRule> rules = new ArrayList<>();
        rules.add(new PlayColorRule());
        return rules;
    }
}
