package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;

/**
 * Created by Fabian on 11.11.2017.
 */

public class Card implements ICardDefinition {

    public Card (CardTypes type, CardColors color){
        //This is used for cards which don't need the "wish" to function properly

        this.type = type;
        this.color = color;
        this.wish = CardColors.NONE;
    }

    //type determines what kind of card you play. This can be "zero", "one", "plusTwo", ...
    public CardTypes type;
    //color determines the color of the card. If the card has no color, it is "none"
    public CardColors color;
    //when playing certain cards, you can choose something (e.g. a color)
    //with all other cards, this field is ignored
    public CardColors wish;
}
