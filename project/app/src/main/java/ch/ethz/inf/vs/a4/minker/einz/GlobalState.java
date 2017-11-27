package ch.ethz.inf.vs.a4.minker.einz;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by Josua on 11/19/17.
 */

public class GlobalState {
    /*
    discard pile
    activePlayer
    nextPlayer
    order
    allPlayers
    gameFinished

*/

    public int cardsToDraw = 1;


    public Player nextPlayer;
    public List<Player> players;

    private Player activePlayer;

    private List<Card> drawPile;
    private List<Card> discardPile;

    private boolean restrictive = false;

    private final int maxDiscardPileSize;

    /**
     * Creates an instance of a Global game state
     * @param maxDiscardPilesize
     */
    public GlobalState(int maxDiscardPilesize){
        this.maxDiscardPileSize = maxDiscardPilesize;
        drawPile = new LinkedList<>();
    }

    /**
     * Creates an instance of a Global game state initialized with an initial card
     * @param maxDiscardPileSize Sets the maximum size of the Discard Pile
     * @param startCard initializes the gameState with a start card on the discard pile
     */
    public GlobalState(int maxDiscardPileSize, Card startCard){
        this(maxDiscardPileSize);
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
     * Draws a card from the drawPile and returns it. The card will be removed from the DrawPile. If
     * the drawPile is empty null is returned
     * @return The drawn Card or null if the drawPile is empty.
     */
    public Card drawCard(){
        if(drawPile.size() == 0){
            return null;
        }
        return drawPile.remove(0);
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

    public void nextTurn(boolean forwards){
        activePlayer = nextPlayer;
        int playerIndex = players.indexOf(nextPlayer);
        if(forwards){
            nextPlayer = players.get((playerIndex + 1) % players.size());
        } else {
            nextPlayer = players.get((playerIndex + players.size() - 1) % players.size());
        }
    }
}
