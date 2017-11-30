package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

import org.json.JSONException;
import org.json.JSONObject;

public class EinzMessageHeader {

    public String messagegroup;
    public String messagetype;

    /**
     * Initialize EinzMessageHeader with arguments.
     * EinzMessageHeader should always have the same format for all messages, because the Header in the messages should also always be of the same format.
     * @param messagegroup The group this message belongs to. E.g. "registration"
     * @param messagetype The specific text of the message. E.g. "RegisterResponse"
     */
    public EinzMessageHeader(String messagegroup, String messagetype){
        this.messagegroup = messagegroup;
        this.messagetype = messagetype;
    }

    /**
     * @return this header encoded as JSONObject, ready to be included into a message like "header":{this return}
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonHeader = new JSONObject();
        jsonHeader.put("messagegroup", this.messagegroup);
        jsonHeader.put("messagetype", this.messagetype);
        return jsonHeader;
    }
}
