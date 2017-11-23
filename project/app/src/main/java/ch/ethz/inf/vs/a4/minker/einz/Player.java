package ch.ethz.inf.vs.a4.minker.einz;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.PlayerState;

/**
 * Created by Fabian on 10.11.2017.
 */

public class Player implements PlayerDefinition {

    private String name;
    //private PlayerState playerState = new PlayerState();

    public ArrayList<Card> hand; //Changed from Hashset since i need to be able to have the same card twice in hand


    public Player(String name){
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

}
