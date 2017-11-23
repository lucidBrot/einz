package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONObject;

public abstract class BasicRule {

    public abstract String getName();

    public abstract String getDescription();


    public boolean isValidKickPlayer(GlobalState state, Player toKick){
        return false;
    }

    public boolean isValidLeaveGame(GlobalState state, Player leaves){
        return false;
    }

    public boolean isValidDrawCards(GlobalState state){
        return false;
    }

    public boolean isValidPlayCard(GlobalState state, Card played){
        return false;
    }

    public boolean isPlayerFinished(GlobalState state, Card played){
        return false;
    }


    public GlobalState onKickPlayer(GlobalState state){
        return state;
    }

    public GlobalState onLeaveGame(GlobalState state){
        return state;
    }

    public GlobalState onDrawCard(GlobalState state){
        return state;
    }

    public GlobalState onPlayCard(GlobalState state){
        return state;
    }

    public GlobalState onPlayerFinished(GlobalState state){
        return state;
    }

}
