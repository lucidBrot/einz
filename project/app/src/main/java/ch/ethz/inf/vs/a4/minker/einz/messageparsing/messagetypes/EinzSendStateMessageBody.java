package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.PlayerState;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;

/**
 * Created by silvia on 11/17/17.
 */

public class EinzSendStateMessageBody extends EinzMessageBody {

    private final GlobalState globalstate;
    private final PlayerState playerstate;

    public GlobalState getGlobalstate() {
        return globalstate;
    }

    public PlayerState getPlayerState() {
        return playerstate;
    }

    public EinzSendStateMessageBody(GlobalState globalstate, PlayerState playerstate) {
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

