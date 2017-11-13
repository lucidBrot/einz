package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;

/**
 * Created by Fabian on 11.11.2017.
 */

public class Card implements ICardDefinition {

    public Card (String type, String color){
        //This is used for cards which don't need the "wish" to function properly

        this.type = type;
        this.color = color;
        this.wish = "";
    }

    public Card (String type, String color, String wish){
        //This is used for cards which need the "wish" to function properly
        this.type = type;
        this.color = color;
        this.wish =  wish;
    }


    //type determines what kind of card you play. This can be "zero", "one", "plusTwo", ...
    public String type;
    //color determines the color of the card. If the card has no color, it is "none"
    public String color;
    //when playing certain cards, you can choose something (e.g. a color)
    //with all other cards, this field is ignored
    public String wish;
}
