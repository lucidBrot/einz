package ch.ethz.inf.vs.a4.minker.einz.model.cards;

/**
 * Created by Fabian on 13.11.2017.
 */

public enum CardText {
    ZERO("ZERO", "0"),
    ONE("ONE", "1"),
    TWO("TWO", "2"),
    THREE("THREE", "3"),
    FOUR("FOUR", "4"),
    FIVE("FIVE", "5"),
    SIX("SIX", "6"),
    SEVEN("SEVEN", "7"),
    EIGHT("EIGHT", "8"),
    NINE("NINE", "9"),
    PLUSTWO("PLUSTWO", "take2"),
    SWITCHORDER("SWITCHORDER", "rev"),
    STOP("STOP", "skip"),
    CHANGECOLOR("CHANGECOLOR", "choose"),
    CHANGECOLORPLUSFOUR("CHANGECOLORPLUSFOUR" ,"take4"),
    DEBUG("DEBUG", "debug"); //TODO: Number of "special cards" might change

    public String type, indicator;
    CardText(String type, String indicator){
        this.type = type;
        this.indicator = indicator;
    }
}
