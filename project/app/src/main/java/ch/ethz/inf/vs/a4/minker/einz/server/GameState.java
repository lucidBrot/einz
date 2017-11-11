package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardAttributeList;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Player;

import static ch.ethz.inf.vs.a4.minker.einz.CardAttributeList.intToColor;

/**
 * Created by Fabian on 09.11.2017.
 */

public class GameState {

    public GameState(){
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
        drawPile = new ArrayList<>();
        playPile = new ArrayList<>();
        order = 1;
        activePlayer = 0;
        threatenedCards = 1;

        drawPile.ensureCapacity(120);
        //go over the colored cards and add them to the deck twice each
        String type,color;
        for (int i = 0; i < 13; i++){
            type = CardAttributeList.intToType(i);
            for (int j = 0; j < 4; j++){
                color = CardAttributeList.intToColor(j);
                for (int k = 0; k < 2; k++){
                    Log.i("dbgng2", "("+type+", "+color+", "+""+")");
                    Card card = new Card(type, color);
                    Log.i("dbgng2", "("+card.type+", "+card.color+", "+card.wish+")");
                    addCardToDrawPile(card);
                }
            }
        }
        //add the none-colored cards 8 times each
        color = CardAttributeList.none;
        for (int i = 14; i < 16; i++){
            type = CardAttributeList.intToType(i);
            for (int j = 0; j < 8; j++){
                addCardToDrawPile(new Card(type, "none", "none"));
            }

        }
        Collections.shuffle(drawPile);

        addPlayer("1000", "Peter");
        addPlayer("1200", "Paul");

        for (int i=0; i < getNumberOfPlayers(); i++){
            for (int j=0; j < 7; j++) {
                drawOneCard(players.get(i));
            }
        }

        playPile.ensureCapacity(1);
        playPile.add(drawPileTopCard());
        numberOfCardsInPlayPile++;
        drawPile.remove(numberOfCardsInDrawPile - 1);
        numberOfCardsInDrawPile--;
    }

    private HashMap<String,String> playerIPs = new HashMap<>(); // Storing players so that they can be identified by IP address

    //Array of all players in order in which they play
    private ArrayList<Player> players = new ArrayList<>(); // I changed this from HashMap to Array, because why would you use a hashmap if every user has an index as identifier?

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
    private ArrayList<Card> drawPile = new ArrayList<>();
    private int numberOfCardsInDrawPile;
    private ArrayList<Card> playPile = new ArrayList<>();
    private int numberOfCardsInPlayPile;

    //Server has to know which player has what cards in hand
    //this is currently not used, server gets cards over player.Hand in players
    private HashMap<Player, HashSet<Card>> playerHands = new HashMap<>();

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

    public Card topCard() {
        return playPile.get(numberOfCardsInPlayPile - 1);
    }
    public Card drawPileTopCard() {
        return drawPile.get(numberOfCardsInDrawPile - 1);
    }

    public Card drawOneCard(Player p){
        Card result = drawPileTopCard();
        drawPile.remove(drawPileTopCard());
        numberOfCardsInDrawPile--;
        Log.i("dbgng3", p.toString());
        p.Hand.add(result);
        return result;
    }

    public HashMap<Integer, Card> playedCards(int x) {
        x = Math.min(x, numberOfCardsInPlayPile);
        HashMap<Integer, Card> result = new HashMap<>(x);
        for (int i = 0; i < x; i++) {
            result.put(i, playPile.get(numberOfCardsInPlayPile - 1 - i));
        }
        return result;
    }

    public Player getActivePlayer() {
        return players.get(activePlayer);
    }

    public void playCardFromHand (Card card, Player p){
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

    public void addCardToDrawPile(Card card){
        drawPile.ensureCapacity(numberOfCardsInDrawPile + 1);
        drawPile.add(card);
        numberOfCardsInDrawPile++;
        Log.i("dbgng", "totalCards: "+Integer.toString(numberOfCardsInDrawPile)+", topCard: (" + drawPileTopCard().type +", "+drawPileTopCard().color+", "+drawPileTopCard().wish+")");
    }
}
