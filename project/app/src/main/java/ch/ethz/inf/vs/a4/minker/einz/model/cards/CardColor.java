package ch.ethz.inf.vs.a4.minker.einz.model.cards;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum CardColor {
    YELLOW("YELLOW"),
    RED("RED"),
    BLUE("BLUE"),
    GREEN("GREEN"),
    NONE("NONE");
    public String color;
    CardColor(String color){
        this.color = color;
    }
}
