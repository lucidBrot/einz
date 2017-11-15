package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum CardColors {
    YELLOW("yellow"),
    RED("red"),
    BLUE("blue"),
    GREEN("green"),
    NONE("none");
    public String color;
    CardColors (String color){
        this.color = color;
    }
}
