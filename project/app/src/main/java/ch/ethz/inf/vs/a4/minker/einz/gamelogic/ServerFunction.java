package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardColors;
import ch.ethz.inf.vs.a4.minker.einz.CardTypes;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Spectator;

/**
 * Created by Fabian on 23.11.2017.
 */

public class ServerFunction implements ServerFunctionDefinition {

    private GlobalState globalState;

    /**
     * @param players the players in the game, the players play in the order in which they are in the
     * ArrayList (lowest index plays first)
     */
    public void initialiseStandardGame(ArrayList<Player> players){
        Queue <Card> drawPile = createStandardDeck();
        Stack<Card> discardPile = new Stack<>();
        discardPile.add(drawPile.poll());
        globalState = new GlobalState(drawPile, discardPile);


    }

    /**
     * initialises a new game with standard cards and rules
     * @param players the players in the game, the players play in the order in which they are in the
     * ArrayList (lowest index plays first)
     * @param spectators the spectators in the game
     */
    public void initialiseStandardGame(ArrayList<Player> players, HashSet<Spectator> spectators){
        // TODO: Fabian. Wieso eine arraylist und ein hashset?
        // Weil wir die Spieler in geordneter Reihenfolge brauchen, die Zuschauer aber nicht.

    }

    /**
     * initialises a new game
     * @param players the players in the game, the players play in the order in which they are in the
     * ArrayList (lowest index plays first)
     * @param deck contains the specified cards the specified amount of times
     * in the HashMap, the Key determines the Card and the Mapped value determines how many times
     * that card is put into the game
     * @param rules set of rules with which the game is played
     */
    public void initialiseGame(ArrayList<Player> players, HashMap<Card, Integer> deck, Collection rules){

    }

    /**
     * initialises a new game
     * @param players the players in the game, the players play in the order in which they are in the
     * ArrayList (lowest index plays first)
     * @param spectators the spectators in the game
     * @param deck contains the specified cards the specified amount of times
     * in the HashMap, the Key determines the Card and the Mapped value determines how many times
     * that card is put into the game
     * @param rules set of rules with which the game is played
     */
    public void initialiseGame(ArrayList<Player> players, HashSet<Spectator> spectators, HashMap<Card, Integer> deck, Collection rules){

    }

    /**
     * Don't know yet what this does exactly
     */
    public void startGame(){

    }

    /**
     * player p wants to play a card, his card is only played if the rules allow him to.
     * @param card the card to be played
     * @param p the player that wants to playe a card
     * @return whether the player is allowed to play the card he wants to play or not
     */
    public boolean play(Card card, Player p){
        return false;
    }

    /**
     * @param p the player that wants to draw cards
     * @return the Cards that player draws, otherwise returns null.
     */
    public ArrayList<Card> drawCards(Player p){
        return null;
    }

    /**
     * ends the running game
     * not sure what this does exactly
     */
    public void endGame(){

    }

    /**
     * removes a Player from the game
     * If there are less than two players left after removing the Player, the game is ended automatically.
     * @param p the player to be removed
     */
    public void removePlayer(Player p){

    }

    /**
     * This function is not listed in the interface and is just used inside this class
     *
     * @return a new standard deck consisting of 112 cards
     */
    private Queue<Card> createStandardDeck(){
        ArrayList<Card> cardsToAdd = new ArrayList<>(); //Create Arraylist with cards and only after shuffling dd them to the drawPile Queue
        for (CardTypes ct: CardTypes.values()) {
            if (ct != CardTypes.CHANGECOLOR && ct != CardTypes.CHANGECOLORPLUSFOUR) {
                for (CardColors cc : CardColors.values()) {
                    if (cc != CardColors.NONE) {
                        Card card = new Card(ct, cc);
                        cardsToAdd.add(card);
                        cardsToAdd.add(card);
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    Card card = new Card(ct, CardColors.NONE);
                    cardsToAdd.add(card);
                }
            }
        }
        Collections.shuffle(cardsToAdd);
        Queue<Card> drawPile = new LinkedList<>();
        for(Card c: cardsToAdd){
            drawPile.add(c);
        }
        return drawPile;
    }

}
