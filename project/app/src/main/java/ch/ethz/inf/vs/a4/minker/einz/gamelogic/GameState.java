package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardColors;
import ch.ethz.inf.vs.a4.minker.einz.CardText;
import ch.ethz.inf.vs.a4.minker.einz.temp.Order;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Spectator;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.GlobalState;

/**
 * Created by Fabian on 09.11.2017.
 */

public class GameState {

    //FIELDS

    //Array of all players in order in which they play
    private ArrayList<Player> players = new ArrayList<>(); // I changed this from HashMap to Array, because why would you use a hashmap if every user has an index as identifier?
    //Array of all the spectators
    private HashSet<Spectator> spectators = new HashSet<>();
    //determines if we have to go up or down in the players Array to determine the next player
    private Order order = Order.UP;
    private Integer indexOfActivePlayer = 0;
    private boolean hasDrawn = false;
    //index indicates how many cards lie below the actual card (bottom card has index 0)
    private Stack<Card> drawPile = new Stack<>();
    private ArrayList<Card> playPile = new ArrayList<>();
    //This indicates how many cards a player has to draw if he can't play a card on his turn
    //Special rules apply when this is greater than one (which means there lie some active plusTwo or changeColorPlusFour cards)
    private int threatenedCards = 1;
    //saves the GlobalStateexplicitly to easily send it to Clients wehenver needed
    //TODO: instatiate this upon initialiseGame
    private GlobalState globalState;


    //Conctrsuctors

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
        newStandartDeck();
        addPlayer("Peter");
        addPlayer("Paul");
        for (int i=0; i < players.size(); i++){
            for (int j=0; j < 7; j++) {
                drawOneCard(players.get(i));
            }
        }
        playPile.ensureCapacity(1);
        playPile.add(drawPileTopCard());
        drawPile.remove(drawPile.size() - 1);

    }

    //This is the constructor that gets used when calling serverFunction.initialiseGame
    public GameState (ArrayList<Player> players, HashMap<Card, Integer> deck, int rules){
        //TODO: Constructor (once rules are made)
    }

    //This is the constructor that gets used when calling serverFunction.initialiseStandartGame
    public GameState (ArrayList<Player> newPlayers, HashSet<Spectator> newSpectators){
        newStandartDeck();
        for (Player p: newPlayers){
            players.add(p);
        }
        if (newSpectators != null) {
            for (Spectator s : newSpectators) {
                spectators.add(s);
            }
        }
        for (Player p: players){
            for (int j=0; j < 7; j++) {
                drawOneCard(p);
            }
        }
        playPile.ensureCapacity(1);
        playPile.add(drawPileTopCard());
        drawPile.remove(drawPile.size() - 1);
    }

    //GetterFunctions

    public ArrayList<Player> getPlayers(){
        return players;
    } //TODO; Don't return the private ArrayList
    public Order getOrder(){
        return order;
    }
    public Player getActivePlayer() {
        return players.get(indexOfActivePlayer);
    }
    public boolean getHasDrawn(){
        return hasDrawn;
    }
    public Stack<Card> getDrawPile(){
        return drawPile;
    }
    public ArrayList<Card> getPlayPile(){
        return playPile;
    }
    public int getThreatenedCards() {
        return threatenedCards;
    }

    //SetterFunctions

    public void setHasDrawn(boolean b){
        hasDrawn = b;
    }


    //Other Functions

    public void increaseThreatenedCards(int x){
        threatenedCards = threatenedCards + x;
    }
    public void resetThreatenedCards(){
        threatenedCards = 1;
    }
    public Card playPiletopCard() {
        return playPile.get(playPile.size() - 1);
    }
    public Card drawPileTopCard() {
        return drawPile.peek();
    }

    public Card drawOneCard(Player p){
        /*
         *  for rule in rules:
         *      rule.isValidCardDraw()
         *  drawCard
         *  for rule in rules:
         *      rule.onDrawCard()
         */
        if (drawPile.size() < 1) {
            newStandartDeck();
        }
            Card result = drawPile.pop();
            p.hand.ensureCapacity(p.hand.size() + 1);
            p.hand.add(result);
            return result;
    }

    public ArrayList<Card> playedCards(int x) {
        x = Math.min(x, playPile.size());
        ArrayList<Card> result = new ArrayList<>(x);
        for (int i = 0; i < x; i++) {
            result.add(playPile.get(playPile.size() - 1 - i));
        }
        return result;
    }

    public void playCardFromHand (Card card, Player p){
        if (p.hand.contains(card)){
            p.hand.remove(card);
            playPile.ensureCapacity(playPile.size() + 1);
            playPile.add(card);
        }
    }

    public void nextPlayer() {
        indexOfActivePlayer = indexOfActivePlayer + order.order;
        if (indexOfActivePlayer == -1){
            indexOfActivePlayer = players.size() - 1;
        }
        if (indexOfActivePlayer == players.size()){
            indexOfActivePlayer = 0;
        }
        hasDrawn = false;
    }

    public void switchOrder(){
        if(order == Order.UP){
            order = Order.DOWN;
        } else if (order == Order.DOWN){
            order = Order.UP;
        }
    }

    //adds a player to the game at the end, so players that get added last also play last
    //allows multiple people to have the same name (as long as they have different IPs
    public void addPlayer (String name){
        boolean newName = true;
        for (Player p: players){
            if (p.getName().equals(name)){
                newName = false;
                break;
            }
        }
        if (newName) {
            Player p = new Player(name);
            players.ensureCapacity(players.size() + 1);
            players.add(p);
        }
    }

    public void addPlayer (Player player){
        boolean newName = true;
        for (Player p: players){
            if (p.getName().equals(player.getName())){
                newName = false;
                break;
            }
        }
        if (newName) {
            players.ensureCapacity(players.size() + 1);
            players.add(player);
        }
    }

    public void removePlayer(Player player){
        players.remove(player);
    }

    public void addCardToDrawPile(Card card){
        drawPile.push(card);
    }

    public void playerWon(Player p){
       //TODO: Implement
        // Remove player from the game and save that he is finsihed so we can build a list of
        // finished players at the end of the game
    }

    public void newStandartDeck(){
        //go over the cards and add them to the deck twice each
        for (CardText ct: CardText.values()) {
            if (ct != CardText.CHANGECOLOR && ct != CardText.CHANGECOLORPLUSFOUR) {
                for (CardColors cc : CardColors.values()) {
                    if (cc != CardColors.NONE) {
                        Card card = new Card(ct, cc);
                        drawPile.push(card);
                        drawPile.push(card);
                    }
                }
            } else {
                for (int i = 0; i < 4; i++) {
                    Card card = new Card(ct, CardColors.NONE);
                    drawPile.push(card);
                }
            }
        }
        Collections.shuffle(drawPile);
    }


    // (III) Functions to check rules

    public void cardEffect(Card card){
        switch (card.text){
            case PLUSTWO:
                if (threatenedCards == 1){
                    increaseThreatenedCards(1);
                } else {
                    increaseThreatenedCards(2);
                }
                break;
            case CHANGECOLORPLUSFOUR:
                if (threatenedCards == 1){
                    increaseThreatenedCards(3);
                } else {
                    increaseThreatenedCards(4);
                }
                break;
            case STOP:
                nextPlayer();
                break;
            case SWITCHORDER:
                switchOrder();
                break;
        }
        nextPlayer();
    }

    public boolean normalPlayRules(Card bottomCard, Card topCard){
        switch (topCard.text){
            case CHANGECOLOR:
            case CHANGECOLORPLUSFOUR:
                return true;
            default:
                if (topCard.color == bottomCard.color ||
                        topCard.text == bottomCard.text ||
                        topCard.color == bottomCard.wish){
                    return true;
                } else {
                    return false;
                }
        }
    }

    public boolean specialPlayRules(Card bottomCard, Card topCard){
        switch (topCard.text){
            case PLUSTWO:
                if (bottomCard.text == CardText.PLUSTWO){
                    return true;
                } else {
                    return false;
                }
            case CHANGECOLORPLUSFOUR:
                if (bottomCard.text == CardText.CHANGECOLORPLUSFOUR){
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

    public void updatePlayerState(Player p) {
        //TODO: implement
    }

    public boolean isPlayable (Card card, Player p) {
        boolean pTurn = false, cardLegit = false;
        Card bottomCard = playPiletopCard();
        if (threatenedCards == 1) {
            //These are the regular rules
            cardLegit = normalPlayRules(bottomCard, card);
        } else {
            //These are the rules when an active plusTwo or changeColorPlusFour is in the middle
            cardLegit = specialPlayRules(bottomCard, card);
        }

        if (getActivePlayer().equals(p)) {
            pTurn = true;
        }
        if (cardLegit && pTurn) {
            return true;
        } else {
            return false;
        }
    }

    //TODO: Spectators

}
