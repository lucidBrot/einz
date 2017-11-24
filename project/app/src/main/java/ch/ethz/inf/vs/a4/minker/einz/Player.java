package ch.ethz.inf.vs.a4.minker.einz;

import java.util.ArrayList;

/**
 * Created by Fabian on 10.11.2017.
 */

public class Player extends Participant {

    //private PlayerState playerState = new PlayerState();

    public ArrayList<Card> hand;


    public Player(String name){
        super(name);
        this.hand = new ArrayList<>();
    }
}
