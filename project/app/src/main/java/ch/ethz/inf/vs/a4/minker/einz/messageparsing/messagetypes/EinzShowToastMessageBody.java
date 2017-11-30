package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzShowToastMessageBody extends EinzMessageBody {

    private final String toast;
    private final String from;
    private final HashMap<String, String> style;

    public EinzShowToastMessageBody(String toast, String from, HashMap<String, String> style) {
        this.toast = toast;
        this.from = from;
        this.style = style;
    }

    public String getToast() {
        return toast;
    }

    public String getFrom() {
        return from;
    }

    public HashMap<String, String> getStyle() {
        return style;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("toast", this.getToast());
        jsonObject.put("from", this.getFrom());
        JSONObject styleJSON = new JSONObject();
        HashMap<String, String> style = getStyle();
        for (Map.Entry<String, String> entry : style.entrySet()) {
            String property = entry.getKey();
            String value = entry.getValue();
            styleJSON.put(property, value);
        }
        jsonObject.put("style", styleJSON);
        return jsonObject;
    }
}
/*
{
  "header":{
    "messagegroup":"toast",
    "messagetype":"ShowToast"
  },
  "body":{
    "toast":"сука блиать",
    "from":"josua",
    "style":{"some":"JSONOBJECT"}
  }
}
 */