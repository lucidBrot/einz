package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum Order{
    UP(1),
    DOWN(-1);
    public int order;
    Order (int order){
        this.order = order;
    }
}
