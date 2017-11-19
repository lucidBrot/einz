package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONObject;

public class Rule { // TODO: implement this class @Josua
    private final JSONObject content;
    private final String identifier;
    public Rule(String identifier, JSONObject content) {

        this.content = content;
        this.identifier = identifier;
    }

    public JSONObject getContentAsJSON(){
        return this.content;
    } // whatever belongs within the specifications of a rule

    public String getIdentifier(){
        return this.identifier;
    } // the name of a rule

    // I think we will denote the ruleset in the messages with the format
    /*
    "ruleset":{
        "rule1":{},
        "rule2":{
            "custom":"info"
            },
        "rule3":{
            "a":"4"
        }
    }
     */
    // please return only the content in getContentAsJSON. i.e. not "rule2", only the righthand object
    // this is used in EinzSpecifyRulesMessageBody
}
