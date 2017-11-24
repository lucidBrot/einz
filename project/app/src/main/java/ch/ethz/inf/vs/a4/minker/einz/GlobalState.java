package ch.ethz.inf.vs.a4.minker.einz;

import org.json.JSONObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;


/**
 * Created by Josua on 11/19/17.
 */

public class GlobalState {
    private Stack<Card> discardPile;
    private Queue<Card> drawPile;
    private Player activePlayer;
    private Player nextPlayer;
    private List<Player> players;
    private List<Player> finishedPlayer;
    private boolean isGameRunning;


    public GlobalState(){
        this(new LinkedList<Card>());
    }

    public GlobalState(Collection<Card> drawPile) {
        this(drawPile, new Stack<Card>());
    }

    //Fabian: Changed this to public so I can create a GlobalState with one card already on the discardPile
    public GlobalState(Collection<Card> drawPile, Stack<Card> discardPile) {
        this.discardPile = discardPile;
        this.drawPile = new LinkedList<>(drawPile);

    }

    public List<Card> getDiscardPile() {
        return discardPile.subList(0, discardPile.size() - 1);
    }

    public void playCatd(Card card){
        discardPile.add(card);
    }

    /**
     * @return The Top card of the draw pile or null if the draw pile is empty
     */
    public Card drawCard() {
        if (drawPile.isEmpty()) {
            return null;
        }
        return drawPile.poll();
    }

    public int drawPileSize() {
        return drawPile.size();
    }

    /**
     * Adds cards at the bottom of the draw pile.
     * @param cards Collection of cards to add to the bottom of the draw pile
     */
    public void addCardstoDrawPile(Collection<Card> cards){
        drawPile.addAll(cards);
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Player player) {
        activePlayer = player;
    }

    public JSONObject toJSON() {
        return null;
    }

    public static GlobalState fromJSON(JSONObject jsonObject) {
        return null;
    }
}
