package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;

public class EinzUpdateLobbyListMessageBody extends EinzMessageBody {

    /*{
        "header":{
        "messagegroup":"registration",
                "messagetype":"UpdateLobbyList"
    },
        "body":{
        "lobbylist":[
        {"roger":"player"},
        {"chris":"player"},
        {"table":"spectator"}
    ],
        "admin":"roger"
    }
    }*/

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
        JSONObject lobbylist = new JSONObject();
        for(String s : getLobbylist().keySet()){
            lobbylist.put(s, getLobbylist().get(s));
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
