package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Created by Fabian on 21.11.2017.
 */

public enum PlayerActions {
    CANPLAY("canPlay"), //denotes whether a player can play at least one card
    CANDRAW("canDraw"),
    ENDTURN("endTurn");


    public String action;
    PlayerActions(String action){
        this.action = action;
    }
}
