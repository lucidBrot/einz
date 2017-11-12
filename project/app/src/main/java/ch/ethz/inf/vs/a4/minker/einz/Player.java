package ch.ethz.inf.vs.a4.minker.einz;

import java.util.HashSet;

/**
 * Created by Fabian on 10.11.2017.
 */

public class Player implements PlayerDefinition {

    public Player(String name, String IP){
        this.name = name;
        this.IP = IP;
        this.Hand = new HashSet<>();
    }

    public String name;
    public String IP;
    public HashSet<Card> Hand;

}
