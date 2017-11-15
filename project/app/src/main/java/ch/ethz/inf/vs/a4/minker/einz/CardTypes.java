package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum CardTypes {
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
    CHANGECOLORPLUSFOUR("changeColorPlusFour");

    public String type;
    CardTypes(String type){
        this.type = type;
    }
}
