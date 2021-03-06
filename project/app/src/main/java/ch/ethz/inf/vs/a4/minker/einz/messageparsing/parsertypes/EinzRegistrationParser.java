package ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

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
                return parseKick(message);
            // The following functions should never be received as server, only as client
            case "RegisterFailure":
                return parseRegisterFailure(message);
            case "UpdateLobbyList":
                return parseUpdateLobbyList(message);
            case "UnregisterResponse":
                return parseUnregisterResponse(message);
            case "RegisterSuccess":
                return parseRegisterSuccess(message);
            case "KickFailure":
                return parseKickFailure(message);
            default:
                Log.d("EinzRegistrationParser","Not a valid messagetype "+messagetype+" for EinzRegistrationParser");
                return null;
        }
    }

    private EinzMessage parseKickFailure(JSONObject message) throws JSONException {
        JSONObject body = message.getJSONObject("body");
        String username = body.getString("username");
        String reason = body.getString("reason");
        return new EinzMessage<>(new EinzMessageHeader("registration", "KickFailure"),
                new EinzKickFailureMessageBody(username, reason));
    }

    private EinzMessage parseUnregisterResponse(JSONObject message) throws JSONException {
        JSONObject body = message.getJSONObject("body");
        String username = body.getString("username");
        String reason = body.getString("reason");
        return new EinzMessage<>(new EinzMessageHeader("registration", "UnregisterResponse"),
                new EinzUnregisterResponseMessageBody(username, reason));
    }

    private EinzMessage<EinzUpdateLobbyListMessageBody> parseUpdateLobbyList(JSONObject message) throws JSONException {
        JSONObject body = message.getJSONObject("body");
        String admin = body.getString("admin");
        HashMap<String, String> lobbylist = new HashMap<>();
        JSONArray jsonLobbyList = body.getJSONArray("lobbylist");
        HashMap<String, JSONObject> playerSeatings = new HashMap<>();

        // foreach mapping {name:role}
        for(int i=0; i<jsonLobbyList.length();i++){
            JSONObject obj = jsonLobbyList.getJSONObject(i);
            lobbylist.put(obj.getString("username"), obj.getString("role"));
            playerSeatings.put(obj.getString("username"), obj.optJSONObject("playerSeating"));
        }

        return new EinzMessage<>(
                new EinzMessageHeader("registration", "UpdateLobbyList"),
                new EinzUpdateLobbyListMessageBody(lobbylist, admin, playerSeatings)
        );
    }

    private EinzMessage<EinzRegisterFailureMessageBody> parseRegisterFailure(JSONObject message) throws JSONException {
        JSONObject body = message.getJSONObject("body");
        return new EinzMessage<>(
                new EinzMessageHeader("registration", "RegisterFailure"),
                new EinzRegisterFailureMessageBody(
                        body.getString("role"), body.getString("username"), body.getString("reason")
                )
        );
    }

    private EinzMessage parseKick(JSONObject message) throws JSONException {
        /*{
          "header":{
            "messagegroup":"registration",
            "messagetype":"Kick"
          },
          "body":{
            "username":"that random dude who we didn't want",
          }
        }*/
        return (new EinzMessage<>(
                new EinzMessageHeader("registration", "Kick"),
                new EinzKickMessageBody(message.getJSONObject("body").getString("username"))) );
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
        return new EinzMessage<>(emh, emb);
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
        EinzMessageHeader emh = new EinzMessageHeader("registration", "Register");
        String username, role;
        JSONObject body = message.getJSONObject("body");
        username = body.getString("username");
        role = body.getString("role");
        JSONObject playerSeating = body.optJSONObject("playerSeating");
        EinzMessageBody emb = new EinzRegisterMessageBody(username, role, playerSeating);
        return new EinzMessage<>(emh, emb);
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
            "messagetype":"RegisterSuccess"
          },
          "body":{
            "success":"true",
            "role":"spectator",
            "reason":"this can be anything if success was true"
          }
        }
         */
        EinzMessageHeader emh = new EinzMessageHeader("registration", "RegisterSuccess");
        JSONObject body = message.getJSONObject("body");
        EinzRegisterSuccessMessageBody emb = new EinzRegisterSuccessMessageBody(body.getString("username"), body.getString("role"));
        return new EinzMessage<>(emh, emb);
    }
}
