package ch.ethz.inf.vs.a4.minker.einz.rules;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Josua on 11/22/17.
 */

public class RuleLoader {

    private Map<String, String> ruleMapping;
    private Map<String, String> ruleDescription;


    public RuleLoader(){
        ruleMapping = new HashMap<>();
        ruleDescription = new HashMap<>();
    }

    public BasicRule getInstanceOfRule(String ruleName){
        if(!ruleMapping.containsKey(ruleName)){
            return null;
        }
        try {
            Class ruleClass = Class.forName(ruleMapping.get(ruleName));
            return (BasicRule) ruleClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    public String getDescriptionOfRule(String ruleName){
        if(!ruleDescription.containsKey(ruleName)){
            return null;
        }
        return ruleDescription.get(ruleName);
    }

    public Set<String> getRulesNames(){
        return ruleMapping.keySet();
    }

    public void  loadRules(JSONArray rulesClasses) throws JSONException{
        for(int i = 0; i < rulesClasses.length(); i++){
            try {
                String ruleClassName = rulesClasses.getString(i);
                Class ruleClass = Class.forName(ruleClassName);
                BasicRule rule = (BasicRule) ruleClass.newInstance();
                ruleMapping.put(rule.getName(), ruleClassName);
                ruleDescription.put(rule.getName(), rule.getDescription());
            } catch (ClassNotFoundException |
                    InstantiationException |
                    IllegalAccessException |
                    ClassCastException e){
                e.printStackTrace();
            }
        }
    }
}
