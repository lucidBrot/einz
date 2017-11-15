package ch.ethz.inf.vs.a4.minker.einz;

import java.util.ArrayList;

/**
 * Created by Fabian on 10.11.2017.
 */

public class Player implements PlayerDefinition {

    public Player(String name, String IP){
        this.name = name;
        this.IP = IP;
        this.Hand = new ArrayList<>();
    }

    public String name;
    public String IP;
    public ArrayList<Card> Hand; //Changed from Hashset since i need to be able to have the same card twice in hand

}
