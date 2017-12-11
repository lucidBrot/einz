package ch.ethz.inf.vs.a4.minker.einz.model.cards;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum CardColor { // TODO: should this be lower-case, or should card_definition.json use uppercase? There seems to be a mismatch generating IDs in ServerFunction
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
