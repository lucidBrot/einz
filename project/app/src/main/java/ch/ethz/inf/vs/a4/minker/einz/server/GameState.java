package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.CardAttributeList;
import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;
import ch.ethz.inf.vs.a4.minker.einz.PlayerDefinition;

/**
 * Created by Fabian on 09.11.2017.
 */

public class GameState {

    //TODO:Constructor

    //Array of all players in order in which they play
    //The first player to play has key 0
    //The last player to play has key (numberOfPlayers - 1)
    private HashMap<Integer, PlayerDefinition> players;
    //determines if we have to go up or down in the players Array to determine the next player
    //order is either 1 or -1, nothing else
    private int order;
    private Integer activePlayer;
    private int numberOfPlayers;

    //Key indicates how many cards lie below the actual card (bottom card has key 0)
    private HashMap<Integer, ICardDefinition> drawPile;
    private int numberOfCardsInDrawPile;
    private HashMap<Integer, ICardDefinition> playPile;
    private int numberOfCardsInPlayPile;

    //Server has to know which player has what cards in hand
    private HashMap<PlayerDefinition, HashSet<ICardDefinition>> playerHands;

    //This indicates how many cards a player has to draw if he can't play a card on his turn
    //Special rules apply when this is greater than one (which means there lie some active plusTwo or changeColorPlusFour cards)
    private int threatenedCards;

    public ICardDefinition topCard() {
        return playPile.get(numberOfCardsInPlayPile - 1);
    }

    public HashMap<Integer, ICardDefinition> playedCards(int x) {
        x = Math.min(x, numberOfCardsInPlayPile);
        HashMap<Integer, ICardDefinition> result = new HashMap<>(x);
        for (int i = 0; i < x; i++) {
            result.put(i, playPile.get(numberOfCardsInPlayPile - 1 - i));
        }
        return result;
    }

    public PlayerDefinition getActivePlayer() {
        return players.get(activePlayer);
    }

    public void playCardFromHand (ICardDefinition card, PlayerDefinition p){
        if (playerHands.get(p).contains(card)){
            playerHands.get(p).remove(card);
            playPile.put(numberOfCardsInPlayPile, card);
            numberOfCardsInPlayPile++;
        }
    }

    public void nextPlayer() {
        activePlayer = activePlayer + order;
        if (activePlayer == -1){
            activePlayer = numberOfPlayers - 1;
        }
        if (activePlayer == numberOfPlayers){
            activePlayer = 0;
        }
    }

    public int getThreatenedCards() {
        return threatenedCards;
    }

    public void increaseThreatenedCards(int x){
        threatenedCards = threatenedCards + x;
    }

    public void switchOrder(){
        order = order * (-1);
    }
}
