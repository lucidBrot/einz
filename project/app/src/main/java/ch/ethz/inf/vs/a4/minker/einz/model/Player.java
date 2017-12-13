package ch.ethz.inf.vs.a4.minker.einz.model;

import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fabian on 10.11.2017.
 */

public class Player extends Participant {

    //private PlayerState playerState = new PlayerState();

    public List<Card> hand;

    public List<PlayerAction> actions;


    public Player(String name){
        super(name);
        this.hand = new ArrayList<>();
    }

    /**
     * Like <code>List.remove()</code>, but without needing to globally overwrite equals. Instead, this only compares the ID and removes the first match.
     * <br>Do not use if the cards would be distinguished by more.
     * @param card
     * @return
     */
    public void removeCardFromHandWhereIDMatches(Card card){
        for(Card c : this.hand){
            if(c.getID().equals(card.getID())){
                this.hand.remove(c);
                break;
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
