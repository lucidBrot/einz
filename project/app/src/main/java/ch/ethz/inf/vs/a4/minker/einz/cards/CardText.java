package ch.ethz.inf.vs.a4.minker.einz.cards;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum CardText {
    ZERO("zero"),
    ONE("one"),
    TWO("two"),
    THREE("three"),
    FOUR("four"),
    FIVE("five"),
    SIX("six"),
    SEVEN("seven"),
    EIGHT("eight"),
    NINE("nine"),
    PLUSTWO("plusTwo"),
    SWITCHORDER("switchOrder"),
    STOP("stop"),
    CHANGECOLOR("changeColor"),
    CHANGECOLORPLUSFOUR("changeColorPlusFour"),
    DEBUG("DEBUG");

    public String type;
    CardText(String type){
        this.type = type;
    }
}
