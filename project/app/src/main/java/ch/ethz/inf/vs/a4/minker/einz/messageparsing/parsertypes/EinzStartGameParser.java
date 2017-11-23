package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzInitGameMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzSpecifyRulesMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzStartGameMessageBody;
import org.json.JSONArray;
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
                return parseStartGame(message);
            case "InitGame":
                return parseInitGame(message);
            default:
                Log.d("EinzStartGameParser","Not a valid messagetype "+messagetype+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage<EinzInitGameMessageBody> parseInitGame(JSONObject message) throws JSONException {
        EinzMessageHeader mheader = new EinzMessageHeader("startgame", "InitGame");
        ArrayList<BasicRule> ruleset=new ArrayList<>();
        JSONObject body = message.getJSONObject("body");
        JSONObject jruleset = body.getJSONObject("ruleset");
        for(int i = 0; i<jruleset.names().length(); i++) {
            BasicRule rule = new BasicRule(jruleset.names().getString(i), jruleset.getJSONObject(jruleset.names().getString(i)));
            ruleset.add(rule);
        }
        JSONArray jturnOrder = body.getJSONArray("turn-order");
        ArrayList<String> turnOrder = new ArrayList<>();
        for(int i = 0; i<jturnOrder.length(); i++){
            turnOrder.add(jturnOrder.getString(i));
        }

        EinzInitGameMessageBody mbody = new EinzInitGameMessageBody(ruleset, turnOrder);

        return new EinzMessage<>(mheader, mbody);
    }

    // ignores its input because this message has no body (currently)
    private EinzMessage<EinzStartGameMessageBody> parseStartGame(JSONObject message) {
        EinzMessageHeader header = new EinzMessageHeader("startgame", "StartGame");
        EinzStartGameMessageBody body = new EinzStartGameMessageBody();
        return new EinzMessage<>(header, body);
    }

    private EinzMessage<EinzSpecifyRulesMessageBody> parseSpecifyRules(JSONObject message) throws JSONException {
        JSONObject body = message.getJSONObject("body");
        EinzMessageHeader mheader = new EinzMessageHeader("startgame", "SpecifyRules");
        ArrayList<BasicRule> ruleset=new ArrayList<>();
        JSONObject jruleset = body.getJSONObject("ruleset");
        // there are SO many ways to iterate over a json object
        // https://stackoverflow.com/questions/9151619/how-to-iterate-over-a-jsonobject
        for(int i = 0; i<jruleset.names().length(); i++) {
            BasicRule rule = new BasicRule(jruleset.names().getString(i), jruleset.getJSONObject(jruleset.names().getString(i)));
            ruleset.add(rule);
        }
        EinzSpecifyRulesMessageBody mbody = new EinzSpecifyRulesMessageBody(ruleset);
        return new EinzMessage<>(mheader, mbody);

    }
}
