package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class EinzRegistrationParser extends ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser {

    @Override
    public EinzMessage parse(JSONObject message) throws JSONException {
        String messagetype = message.getJSONObject("header").getString("messagetype");
        switch(messagetype){
            case "Register":
                return parseRegister(message);
            case "UnregisterRequest":
                return parseUnregisterRequest(message);
            case "Kick":
                return parseKick(message); // TODO: parse Kick
            // The following functions should never be received as server, feel free to implement them here though, chris
            /*
            case "RegisterFailure":
                return parseRegisterFailure(message);
            case "UpdateLobbyList":
                return parseUpdateLobbyList(message);
            case "UnregisterResponse":
                return parseUnregisterResponse(message):
            case "RegisterSuccess":
                return parseRegisterSuccess(message);
            */
            default:
                Log.d("EinzRegistrationParser","Not a valid messagetype "+messagetype+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage parseKick(JSONObject message) { // TODO : implement all those cases
        return null;
    }

    private EinzMessage parseUnregisterRequest(JSONObject message) throws JSONException {
        /*
          {
          "header":{
            "messagegroup":"registration",
            "messagetype":"UnregisterRequest"
          },
          "body":{
            "username":"roger"
          }
         */
        EinzMessageHeader emh = new EinzMessageHeader("registration", "UnregisterRequest");
        JSONObject body = message.getJSONObject("body");
        String username = body.getString("username");
        EinzMessageBody emb = new EinzUnregisterRequestMessageBody(username);
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

    /**
     * Serverside parsing when receiving a messagetype <i>Register</i>
     * @param message
     * @return
     * @throws JSONException
     */
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
        String username, role;
        JSONObject body = message.getJSONObject("body");
        username = body.getString("username");
        role = body.getString("role");
        EinzMessageBody emb = new EinzRegisterMessageBody(username, role);
        EinzMessage einzMessage = new EinzMessage(emh, emb);
        return einzMessage;
    }

    /**
     * Clientside
     * @param message
     * @return
     * @throws JSONException
     */
    private EinzMessage parseRegisterSuccess(JSONObject message) throws JSONException {
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
        return null;
    }
}
