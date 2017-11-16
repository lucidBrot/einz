package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Immutable container of an {@link EinzMessageHeader} and an {@link EinzMessageBody}
 */
public class EinzMessage {
    private final EinzMessageHeader header;
    private final EinzMessageBody body;

    /**
     * Create a <i>EinzMessage</i> Object that consists of a Header and a Body
     * @param header Uniform among all messages
     * @param body Specific to the messagetype
     */
    public EinzMessage (EinzMessageHeader header, EinzMessageBody body){
        this.header = header;
        this.body = body;
    }

    public EinzMessageHeader getHeader() {
        return header;
    }

    public EinzMessageBody getBody() {
        return body;
    }

    /**
     * @return this Message JSONencoded and ready to be sent
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject msg = new JSONObject();
        msg.put("header", this.header.toJSON());
        msg.put("body", this.body.toJSON());
        return msg;
    }
}
