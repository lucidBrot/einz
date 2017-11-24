package ch.ethz.inf.vs.a4.minker.einz;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Fabian on 09.11.2017.
 */

public abstract class Participant {

    private String name;

    public Participant(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}
