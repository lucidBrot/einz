package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Rule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EinzStartGameParser extends EinzParser {
    /**
     * @param message JSON-encoded message as defined in protocols/documentation_Messages.md
     * @return an EinzMessage Object containing all the information specific to this kind of message
     */
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        JSONObject header = message.getJSONObject("header");
        String messagetype = header.getString("messagetype");
        switch (messagetype){
            case "SpecifyRules":
                return parseSpecifyRules(message);
            case "StartGame":
                return parseStartGame();
            case "InitGame":
                return parseInitGame();
            default:
                Log.d("EinzStartGameParser","Not a valid messagetype "+messagetype+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage<EinzSpecifyRulesMessageBody> parseSpecifyRules(JSONObject message) throws JSONException {
        JSONObject body = message.getJSONObject("body");
        EinzMessageHeader mheader = new EinzMessageHeader("startgame", "SpecifyRules");
        ArrayList<Rule> ruleset=new ArrayList<>();
        JSONObject jruleset = body.getJSONObject("ruleset");
        // there are SO many ways to iterate over a json object
        // https://stackoverflow.com/questions/9151619/how-to-iterate-over-a-jsonobject
        for(int i = 0; i<jruleset.names().length(); i++) {
            Rule rule = new Rule(jruleset.names().getString(i), jruleset.getJSONObject(jruleset.names().getString(i)));
            ruleset.add(rule);
        }
        EinzSpecifyRulesMessageBody mbody = new EinzSpecifyRulesMessageBody(ruleset);
        return new EinzMessage<>(mheader, mbody);

    }
}
