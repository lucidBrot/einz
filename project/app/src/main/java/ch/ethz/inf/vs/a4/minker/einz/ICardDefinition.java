package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Created by Josua on 11/6/17.
 */

public interface ICardDefinition {

    //type determines what kind of card you play. This can be "zero", "one", "plusTwo", ...
    public String type = "";
    //color determines the color of the card. If the card has no color, it is "none"
    public String color = "";
    //when playing certain cards, you can choose something (e.g. a color)
    //with all other cards, this field is ignored
    public String wish ="";
}
