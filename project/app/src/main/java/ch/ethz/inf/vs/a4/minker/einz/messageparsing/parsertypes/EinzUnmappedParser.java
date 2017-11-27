package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnmappedMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Used as a parser for unmapped parsers. Check for the text of action or just let it run (probably does nothing)
 */
public class EinzUnmappedParser extends EinzParser{
    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        EinzUnmappedMessageBody body = new EinzUnmappedMessageBody(message);
        EinzMessageHeader header = new EinzMessageHeader("unmapped", "unmapped");
        return new EinzMessage<EinzUnmappedMessageBody>(header, body);
    }
}
