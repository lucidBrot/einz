package ch.ethz.inf.vs.a4.minker.einz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Josua on 11/19/17.
 */

public class GlobalState {

    /**
     * Indicates if the play order is Forwards. this means that the list of players is traversed
     * from 0 to players.size().
     */
    public boolean playOrderIsForwards = true;

    /**
     * The Player that is on turn after the activePlayer has finished his turn. May be changed from
     * Rules.
     */
    public Player nextPlayer;


    private Player activePlayer;
    private List<Player> players;

    private List<Card> drawPile;
    private List<Card> discardPile;

    private int cardsToDraw = 1;

    private boolean restrictive = false;

    private final int maxDiscardPileSize;


    /**
     * Creates an instance of a Global game state.
     * @param maxDiscardPileSize Sets the maximum size of the Discard Pile
     * @param orderOfPlayers An ordered list of players determining the order in which the players will play.
     */
    public GlobalState(int maxDiscardPileSize, List<Player> orderOfPlayers){
        this.maxDiscardPileSize = maxDiscardPileSize;
        this.players = orderOfPlayers;
        this.drawPile = new LinkedList<>();
    }

    /**
     * Creates an instance of a Global game state initialized with an initial card. The Order of the
     * players also has to be declared at the beginning of the game.
     * @param maxDiscardPileSize Sets the maximum size of the Discard Pile
     * @param startCard initializes the gameState with a start card on the discard pile
     * @param orderOfPlayers An ordered list of players determining the order in which the players will play.
     */
    public GlobalState(int maxDiscardPileSize, List<Player> orderOfPlayers, Card startCard){
        this(maxDiscardPileSize, orderOfPlayers);
        addCardToDiscardPile(startCard);

    }

    /**
     * Adds the given List of Cards to the Draw pile to refill the draw Pile.
     * @param cards Cards to add to the Draw Pile
     */
    public void addCardsToDrawPile(List<Card> cards){
        drawPile.addAll(cards);
    }

    /**
     * Returns a reference to the discardPile. Rules may change it. The lowermost Card is at index 0
     * and the top card is at the end of the list.
     * @return Reference to the Discardpile
     */
    public List<Card> getDiscardPile(){
        return discardPile;
    }

    /**
     * Returns the top of the discardPile.
     * @return Card on top of the discardPile
     */
    public Card getTopCardDiscardPile(){
        return discardPile.get(discardPile.size() - 1);
    }

    /**
     * Adds many cards to the discard pile. the Cards have to be ordered such that the card that
     * should be on the lowermost position is at index 0. the discard Pile gets trimmed to the
     * macDiscardPileSize.
     * @param cards List of cards to add to the discardPile
     */
    public void addCardsToDiscardPile(List<Card> cards){
        discardPile.addAll(cards);
        discardPile.subList(Math.max(0, discardPile.size() - maxDiscardPileSize), discardPile.size() - 1);
    }

    /**
     * Adds one Card to the discard pile and sets it on top of the pile (at the end of the list)
     * @param card Card to play on top of the discard pile
     */
    public void addCardToDiscardPile(Card card){
        discardPile.add(card);
        if(discardPile.size() > maxDiscardPileSize){
            discardPile = discardPile.subList(1, discardPile.size() - 1);
        }
    }

    /**
     * Draws a given amount of cards from the drawPile. If there are not enough cards on the drawPile
     * null is returned. The client has to fill up the drawPile first before drawing the same amount
     * again. If numberOfCards is smaller than one an empty List is returned.
     * @param numberOfCards Number of Cards to Draw. Has to be greater that zero.
     * @return List of the drawn Cards or null if there were not enough Cards in the drawPile for
     * this query.
     */
    public List<Card> drawCards(int numberOfCards){
        if(numberOfCards < 1){
            return new ArrayList<>();
        }

        if(drawPile.size() < numberOfCards){
            return null;
        }

        List<Card> result = drawPile.subList(0,numberOfCards);
        drawPile = drawPile.subList(numberOfCards, drawPile.size());
        return result;
    }

    public int getCardsToDraw() {
        return cardsToDraw;
    }

    public void setCardsToDraw(int cardsToDraw) {
        if(cardsToDraw >= 0 ) {
            this.cardsToDraw = cardsToDraw;
        }
    }

    /**
     * Changes the validation of the Rules to Restrictive mode. In Restrictive Mode every rule has
     * to return True to be able to perform a certain action.
     */
    public void setRestrictive(){
        restrictive = true;
    }

    /**
     * Changes the validation of the Rules to Permissive mode. In permissive mode at least one rule
     * has to return true in order to perform a certain action.
     * This is the default.
     */
    public void setPermissive(){
        restrictive = false;
    }

    /**
     * Returns whether restrictive mode is enabled or not
     * @return True if restrictive mode is enabled
     */
    public boolean isRestrictive(){
        return restrictive;
    }

    /**
     * Returns the Player who's turn it is currently
     * @return the Player on turn
     */
    public Player getActivePlayer(){
        return activePlayer;
    }

    /**
     * Returns the ordered List of players. Rules are not meant to rearrange the list.
     * @return A copy of the players list
     */
    public List<Player> getPlayersOrdered(){
        return new ArrayList<>(players);
    }

    /**
     * Sets the nextPlayer to the activePlayer and sets the new nextPlayer taking playOrderIsForward
     * into account.
     */
    public void nextTurn(){
        activePlayer = nextPlayer;
        int playerIndex = players.indexOf(nextPlayer);
        if(playOrderIsForwards){
            nextPlayer = players.get((playerIndex + 1) % players.size());
        } else {
            nextPlayer = players.get((playerIndex + players.size() - 1) % players.size());
        }
    }
}
