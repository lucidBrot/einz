package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.HashMap;

import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;
import ch.ethz.inf.vs.a4.minker.einz.PlayerDefinition;

/**
 * Created by Fabian on 09.11.2017.
 */

public class GameState {

    //Array of all players in order in which they play
    private HashMap <Integer,PlayerDefinition> players;
    //determines if we have to go up or down in the players Array to determine the next player
    private int order;
    private Integer activePlayer;
    private int numberOfPlayers;

    //Piles with the format <Position = x-lowest card in the pile, actual Card>
    private HashMap <Integer, ICardDefinition> drawPile;
    private int numberOfCardsInDrawPile;
    private HashMap <Integer, ICardDefinition>  playPile;
    private int numberOfCardsInPlayPile;

    public ICardDefinition topCard(){
        return playPile.get(numberOfCardsInPlayPile);
    }

}
