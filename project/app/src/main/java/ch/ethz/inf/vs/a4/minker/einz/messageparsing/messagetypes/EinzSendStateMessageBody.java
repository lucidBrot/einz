package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.GlobalStateParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.PlayerState;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzSendStateMessageBody extends EinzMessageBody {

    private final GlobalStateParser globalstate;
    private final PlayerState playerstate;

    public GlobalStateParser getGlobalstate() {
        return globalstate;
    }

    public PlayerState getPlayerState() {
        return playerstate;
    }

    public EinzSendStateMessageBody(GlobalStateParser globalstate, PlayerState playerstate){
        this.playerstate = playerstate;
        this.globalstate = globalstate;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject globalstateJSON = getGlobalstate().toJSON();
        JSONObject playerstateJSON = getPlayerState().toJSON();
        jsonObject.putOpt("globalstate", globalstateJSON);
        jsonObject.putOpt("playerstate", playerstateJSON);
        return jsonObject;
    }
}

/*
{
  "header":{
    "messagegroup":"stateinfo",
    "messagetype":"SendState"
  },
  "body":{
    "globalstate":{
      "numcardsinhand":{
        "Eric":"3",
        "Rip":"100",
        "Ric":"2"
      },
      "stack":[
        {"ID":"cardID01", "origin":"~talon"},
        {"ID":"cardID1337", "origin":"Rip"}
      ],
      "whoseturn":"Ric",
      "drawxcardsmin":"2"
    },
    "playerstate":{
      "hand":[
        {"ID":"cardID03", "origin":"Eric"}
      ],
      "possibleactions":
        [
        "leaveGame", "drawCards", "playCard"
        ]
    }
  }
}
 */
