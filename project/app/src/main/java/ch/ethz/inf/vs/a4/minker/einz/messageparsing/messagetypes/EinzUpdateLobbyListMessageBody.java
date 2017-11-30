package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

public class EinzUpdateLobbyListMessageBody extends EinzMessageBody {

    /*
    {
  "header":{
    "messagegroup":"registration",
    "messagetype":"UpdateLobbyList"
  },
  "body":{
    "lobbylist":[
      {"username":"roger", "role":"player"},
      {"username":"chris", "role":"player"},
      {"username":"table", "role":"spectator"}
    ],
    "admin":"roger"
  }
    */

    private final HashMap<String, String> lobbylist;
    private final String admin;

    public EinzUpdateLobbyListMessageBody(HashMap<String, String> lobbylist, String admin){

        this.lobbylist = lobbylist;
        this.admin = admin;
    }

    /**
     * @return the body as JSONobject, ready to be included as "body":{this returned Object} in a message
     */
    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject body = new JSONObject();
        JSONArray lobbylist = new JSONArray();
        for(String s : getLobbylist().keySet()){
            JSONObject entry = new JSONObject();
            entry.put("username", s);
            entry.put("role", getLobbylist().get(s));
            lobbylist.put(entry);
        }
        body.put("lobbylist", lobbylist);
        body.put("admin", getAdmin());
        return body;
    }

    public HashMap<String, String> getLobbylist() {
        return lobbylist;
    }

    public String getAdmin() {
        return admin;
    }
}
