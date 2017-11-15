package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzJsonMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzRegistrationParser extends ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser {

    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch(messagetype){
            case "Register":
                return parseRegister(message);
            case "RegisterResponse":
                return parseRegisterResponse(message);
            default:
                Log.d("EinzRegistrationParser","Not a valid messagetype "+messagetype+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage parseRegister (JSONObject message) throws JSONException {
        /*
        {
          "header":{
            "messagegroup":"registration",
            "messagetype":"Register"
          },
          "body":{
            "username":"roger",
            "role":"player"
          }
        }
         */
        EinzMessageHeader emh = new EinzMessageHeader("registration", "register");
        EinzMessageBody emb = new EinzJsonMessageBody(message.getJSONObject("body")); // TODO: EinzRegisterMessageBody
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

    private EinzMessage parseRegisterResponse(JSONObject message) throws JSONException {
        /*
        {
          "header":{
            "messagegroup":"registration",
            "messagetype":"RegisterResponse"
          },
          "body":{
            "success":"true",
            "role":"spectator",
            "reason":"this can be anything if success was true"
          }
        }
         */
        EinzMessageHeader emh = new EinzMessageHeader("registration", "RegisterResponse");
        EinzMessageBody emb = new EinzJsonMessageBody(message.getJSONObject("body")); // TODO: EinzRegisterResponseMessageBody
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }
}
