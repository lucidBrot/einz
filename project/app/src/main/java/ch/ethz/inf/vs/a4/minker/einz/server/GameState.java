package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.CardAttributeList;
import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.PlayerDefinition;

/**
 * Created by Fabian on 09.11.2017.
 */

public class GameState {

    public void GameState(){
    /*
    This constructor is used for some testing and as sort of a blueprint for more "useful" constructors.
    This constructor is initialising a standart game of Einz with:
    -a (shuffled) deck of 120 cards
    -extra cards "plusTwo", "switchOrder", "stop", "changeColor", "changeColorPlusFour"
    -each card exists exactly 2 times, except "changeColor" and "changeColorPlusFour" which exist 8 times each
    -two players ("1000", "Peter"), ("1200", "Paul")
    -each players gets a starting hand of 7 cards
    -the top card from the drawPile gets put onto the (empty) playPile and is used as the starting card
     */




    }
    //TODO:Constructor

    public GameState(){
        this.playerIPs = new HashMap<>();
    }

    private HashMap<String,String> playerIPs = new HashMap<>(); // Storing players so that they can be identified by IP address

    //Array of all players in order in which they play
    private ArrayList<PlayerDefinition> players = new ArrayList<>(); // I changed this from HashMap to Array, because why would you use a hashmap if every user has an index as identifier?

    //determines if we have to go up or down in the players Array to determine the next player
    //order is either 1 or -1, nothing else
    private int order;
    private Integer activePlayer;
    private int numberOfPlayers;

    public int getNumberOfPlayers(){
        return numberOfPlayers;
    }
    public void setNumberOfPlayers(int x){
        numberOfPlayers = x;
    }

    //index indicates how many cards lie below the actual card (bottom card has index 0)
    private ArrayList<ICardDefinition> drawPile = new ArrayList<>();
    private int numberOfCardsInDrawPile;
    private ArrayList<ICardDefinition> playPile = new ArrayList<>();
    private int numberOfCardsInPlayPile;

    //Server has to know which player has what cards in hand
    private HashMap<PlayerDefinition, HashSet<ICardDefinition>> playerHands = new HashMap<>();

    //This indicates how many cards a player has to draw if he can't play a card on his turn
    //Special rules apply when this is greater than one (which means there lie some active plusTwo or changeColorPlusFour cards)
    private int threatenedCards;
    //Functions to manipulate threatenedCards
    public int getThreatenedCards() {
        return threatenedCards;
    }
    public void increaseThreatenedCards(int x){
        threatenedCards = threatenedCards + x;
    }
    public void resetThreatenedCards(){
        threatenedCards = 1;
    }
    public void setThreatenedCards(int x){
        threatenedCards = x;
    }

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
            playPile.set(numberOfCardsInPlayPile, card);
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

    public void switchOrder(){
        order = order * (-1);
    }

    //adds a player to the game at the end, so players that get added last also play last
    public void addPlayer (String IP, String name){
        if (!playerIPs.containsKey(IP)) {
            playerIPs.put(IP, name);
            Player p = new Player(name, IP);
            players.ensureCapacity(numberOfPlayers + 1);
            players.add(numberOfPlayers, p);
            numberOfPlayers++;
        }
    }

    public void removePlayer(String IP){
        if (playerIPs.containsKey(IP)) {
            playerIPs.remove(IP);
            for (int i = 0; i < numberOfPlayers; i++){
                if (players.get(i).IP.equals(IP)){
                    players.remove(i);
                }
            }
            numberOfPlayers--;
        }

    }
}
