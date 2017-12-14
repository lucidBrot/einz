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
    private final HashMap<String, JSONObject> playerSeatings;

    public EinzUpdateLobbyListMessageBody(HashMap<String, String> lobbylist, String admin){

        this(lobbylist, admin, null);
    }

    public EinzUpdateLobbyListMessageBody(HashMap<String, String> lobbylist, String admin, HashMap<String, JSONObject> playerToPlayerSeating){
        this.playerSeatings = playerToPlayerSeating;
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

        HashMap<String, JSONObject> playerSeatings;
        if(this.playerSeatings == null){
            playerSeatings = new HashMap<>();
        } else {
            playerSeatings = this.playerSeatings;
        }

        for(String s : getLobbylist().keySet()){
            JSONObject entry = new JSONObject();
            entry.put("username", s);
            entry.put("role", getLobbylist().get(s));
            entry.put("playerSeating", playerSeatings.get(s));
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

    public HashMap<String, JSONObject> getPlayerSeatings() {
        return playerSeatings;
    }
}
