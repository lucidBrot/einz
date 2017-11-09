package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.ArrayList;
import java.util.HashMap;

import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;
import ch.ethz.inf.vs.a4.minker.einz.PlayerDefinition;

/**
 * Created by Fabian on 09.11.2017.
 */

public class GameState {

    public GameState(){
        this.playerIPs = new HashMap<>();
    }

    private HashMap<String,String> playerIPs; // Storing players so that they can be identified by IP address

    //Array of all players in order in which they play
    private ArrayList<PlayerDefinition> players; // I changed this from HashMap to Array, because why would you use a hashmap if every user has an index as identifier?
    //determines if we have to go up or down in the players Array to determine the next player
    private int order;
    private Integer activePlayer;
    private int numberOfPlayers;

    //Key indicates how many cards lie below the actual card (bottom card has key 0)
    private HashMap <Integer, ICardDefinition> drawPile;
    private int numberOfCardsInDrawPile;
    private HashMap <Integer, ICardDefinition>  playPile;
    private int numberOfCardsInPlayPile;

    public ICardDefinition topCard(){
        return playPile.get(numberOfCardsInPlayPile - 1);
    }

    public HashMap <Integer, ICardDefinition> playedCards(int x){
        x = Math.min(x, numberOfCardsInPlayPile);
        HashMap <Integer, ICardDefinition> result = new HashMap<>(x);
        for (int i = 0; i < x; i++){
            result.put(i, playPile.get(numberOfCardsInPlayPile - 1 - i));
        }
        return result;
    }

    public PlayerDefinition getActivePlayer(){
        return players.get(activePlayer);
    }

}
