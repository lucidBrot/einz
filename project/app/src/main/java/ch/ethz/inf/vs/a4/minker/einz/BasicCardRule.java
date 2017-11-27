package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Basic Rule Class for rules applicable to cards.
 */
public abstract class BasicCardRule extends BasicRule{

    protected Card assignedTo;

    public BasicCardRule(GameConfig config, Card assignedTo) {
        super(config);
        this.assignedTo = assignedTo;
    }


    public boolean isValidKickPlayerPermissive(GlobalState state, Player toKick){
        return false;
    }
    public boolean isValidKickPlayerRestrictive(GlobalState state, Player toKick){
        return true;
    }

    public boolean isValidLeaveGamePermissive(GlobalState state, Player leaves){
        return false;
    }
    public boolean isValidLeaveGameRestrictive(GlobalState state, Player leaves){
        return true;
    }

    public boolean isValidDrawCardsPermissive(GlobalState state){
        return false;
    }
    public boolean isValidDrawCardsRestrictive(GlobalState state){
        return true;
    }

    public boolean isValidPlayCardPermissive(GlobalState state, Card played){
        return false;
    }
    public boolean isValidPlayCardRestrictive(GlobalState state, Card played){
        return true;
    }

    public boolean isPlayerFinishedPermissive(GlobalState state, Card played){
        return false;
    }
    public boolean isPlayerFinishedRestrictive(GlobalState state, Card played){
        return true;
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

    public GlobalState onPlayCard(GlobalState state, Card played){
        return state;
    }

    public GlobalState onPlayerFinished(GlobalState state){
        return state;
    }



}
