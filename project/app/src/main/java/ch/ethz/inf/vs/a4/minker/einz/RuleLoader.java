package ch.ethz.inf.vs.a4.minker.einz;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.model.ParameterType;
import ch.ethz.inf.vs.a4.minker.einz.model.ParametrizedRule;

/**
 * Created by Josua on 11/22/17.
 */

public class RuleLoader {

    private Map<String, String> ruleMapping;
    private Map<String, String> ruleDescription;
    private Map<String, Map<String, ParameterType>> ruleParameters;


    public RuleLoader(){
        ruleMapping = new HashMap<>();
        ruleDescription = new HashMap<>();
        ruleParameters = new HashMap<>();
    }

    public BasicRule getInstanceOfRule(String ruleName){
        if(!ruleMapping.containsKey(ruleName)){
            return null;
        }
        try {
            Class ruleClass = Class.forName(ruleMapping.get(ruleName));
            BasicRule b = (BasicRule) ruleClass.newInstance();
            return b;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }

    public Set<String> getRulesNames(){
        return ruleMapping.keySet();
    }

    public String getDescriptionOfRule(String ruleName){
        if(!ruleDescription.containsKey(ruleName)){
            return null;
        }
        return ruleDescription.get(ruleName);
    }

    public Map<String, ParameterType> getParameters(String ruleName){
        if(!ruleParameters.containsKey(ruleName)){
            return null;
        }
        return ruleParameters.get(ruleName);
    }

    public void  loadRules(JSONArray rulesClasses) throws JSONException{
        for(int i = 0; i < rulesClasses.length(); i++){
            try {
                String ruleClassName = rulesClasses.getString(i);
                Class ruleClass = Class.forName(ruleClassName);
                BasicRule rule = (BasicRule) ruleClass.newInstance();
                ruleMapping.put(rule.getName(), ruleClassName);
                ruleDescription.put(rule.getName(), rule.getDescription());
                if(rule instanceof ParametrizedRule) {
                    ruleParameters.put(rule.getName(), ((ParametrizedRule)rule).getParameterTypes());
                }
            } catch (ClassNotFoundException |
                    InstantiationException |
                    IllegalAccessException |
                    ClassCastException e){
                e.printStackTrace();
            }
        }
    }

    public void loadRulesFromResourceFile(Context applicationContext, int resourceFile) throws JSONException {
        InputStream jsonStream = applicationContext.getResources().openRawResource(resourceFile);
        JSONArray jsonArray = new JSONArray(convertStreamToString(jsonStream));
        this.loadRules(jsonArray);
    }

    // https://stackoverflow.com/questions/6774579/typearray-in-android-how-to-store-custom-objects-in-xml-and-retrieve-them
    // utility function
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
