package ch.ethz.inf.vs.a4.minker.einz.model;

/**
 * Created by Josua on 11/25/17.
 */

public enum PlayerAction {

    LEAVE_GAME("leaveGame"),
    DRAW_CARDS("drawCards"),
    KICK_PLAYER("kickPlayer"),
    PLAY_CARD("playCard"),
    FINISH_TURN("finishTurn");

    public String name;
    PlayerAction(String name){
        this.name = name;
    }
}
