package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import ch.ethz.inf.vs.a4.minker.einz.cards.Card;

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

}
