package ch.ethz.inf.vs.a4.minker.einz.model;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;


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
    private List<PlayerContainer> players;

    private List<Player> finishedPlayers;

    private List<Card> drawPile;
    private List<Card> discardPile;

    private int cardsToDraw = 1;

    private boolean gameFinished = false;

    private final int maxDiscardPileSize;

    private HashMap<String, Integer> points = new HashMap<String, Integer>(); // String for player.getName(),  String for points

    /**
     * Creates an instance of a Global game state.
     * @param maxDiscardPileSize Sets the maximum size of the Discard Pile
     * @param orderOfPlayers An ordered list of players determining the order in which the players will play.
     */
    public GlobalState(int maxDiscardPileSize, List<Player> orderOfPlayers){
        if(Debug.smallStack>=0){
            Log.w("DEBUG", "Using a small stack max size of "+Debug.smallStack+". To disable, set Debug.smallStack to -1");
            this.maxDiscardPileSize = Debug.smallStack;
        } else {
            this.maxDiscardPileSize = maxDiscardPileSize;
        }

        this.players = new ArrayList<>();
        for (Player player : orderOfPlayers) {
            players.add(new PlayerContainer(player));
        }
        this.finishedPlayers = new ArrayList<>();
        this.drawPile = new LinkedList<>();
        this.discardPile = new LinkedList<>();
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

    private GlobalState(){
        maxDiscardPileSize = 0;
    }

    /**
     * Adds the given List of Cards to the Draw pile to refill the draw Pile.
     * @param cards Cards to add to the Draw Pile
     */
    public void addCardsToDrawPile(List<Card> cards){
        drawPile.addAll(cards);
    }

    /**
     * @return a <literal>HashMap<name of player, points></literal>
     */
    public HashMap<String, Integer> getPoints() {
        return points;
    }

    public void setPoints(HashMap<String, Integer> points) {
        this.points = points;
    }

    /**
     * Adds the (possibly negative) points to the score of the player identified by his name.
     * If there were no points previously, it will set them to the new value
     */
    public void addPoints(String playerName, Integer points){
        if(this.getPoints().containsKey(playerName)){
            this.getPoints().put(playerName, this.getPoints().get(playerName)+ points);
        } else {
            this.getPoints().put(playerName, points);
        }
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
            discardPile = discardPile.subList(1, discardPile.size());
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

    /**
     * Returns the amount that a player has to draw if he decides to draw cards.
     * @return Amount of cards to draw.
     */
    public int getCardsToDraw() {
        return cardsToDraw;
    }

    /**
     * Sets the amount of cards a player has to draw if he decides to draw cards. The value has to
     * be non-negative.
     * @param cardsToDraw Amount the next player should draw. Should be greater equal zero.
     */
    public void setCardsToDraw(int cardsToDraw) {
        if(cardsToDraw >= 0 ) {
            this.cardsToDraw = cardsToDraw;
        }
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
        ArrayList<Player> result = new ArrayList<>();
        for (PlayerContainer container : players) {
            result.add(container.player);
        }
        return result;
    }

    /**
     * Returns an ordered Map of the players. This Method is intended for the Client to get a mapping
     * between a player name and his hand size. The key set is ordered as the in getPlayersOrdered
     * method.
     * @return ordered map to map player name to numbers of cards
     */
    public LinkedHashMap<String, Integer> getPlayerHandSizeOrdered(){
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (PlayerContainer container : players) {
            result.put(container.name, container.getNumCardsInHand());
        }
        return result;
    }

    /**
     * Returns if the game should be ended. Is set to true if a rule decides to end the game.
     * @return True if the game has finished.
     */
    public boolean isGameFinished(){
        return gameFinished;
    }

    /**
     * Sets the finished Flag to true.
     */
    public synchronized void finishGame(){
        gameFinished = true;
    }

    /**
     * adds the specified player to the finished players and removes any players with the same name from the list of players.
     * Does both only if there was at least one player with that name in that list.
     * <b>Make sure the actual reallife person players have unique names or rewrite this message</b>
     */
    public synchronized void setPlayerFinished(Player player){
        for(PlayerContainer pc : players){
            if(pc.player.getName().equals(player.getName())){
                this.players.remove(pc);
                this.finishedPlayers.add(player);
            }
        }
    }

    public List<Player> getFinishedPlayers(){
        return new ArrayList<>(finishedPlayers);
    }

    public synchronized void removePlayer(Player player){
        for (int i = 0; i < players.size(); i++) {
            PlayerContainer container = players.get(i);
            if (container.player == player){
                players.remove(container);
                break;
            }
        }
    }

    /**
     * Sets the nextPlayer to the activePlayer and sets the new nextPlayer taking playOrderIsForward
     * into account.
     */
    public void nextTurn(){
        activePlayer = nextPlayer;

        //Changed this loop so it owrks with PlayerContainer <-> Player
        int playerIndex = -1;
        for(int i=0; i < players.size(); i++){
            if(players.get(i).player.equals(nextPlayer)){
                playerIndex = i;
            }
        }

        if(playOrderIsForwards){
            nextPlayer = players.get((playerIndex + 1) % players.size()).player;
        } else {
            nextPlayer = players.get((playerIndex + players.size() - 1) % players.size()).player;
        }
    }


    /**
     * Serializes the GlobalState object to JSON.
     * @return To JSON serialized GlobalState object
     */
    public JSONObject toJSON() throws JSONException{

        JSONArray numCardsInHand = new JSONArray();
        for (PlayerContainer playerContainer : players){
            JSONObject player = new JSONObject();
            player.put("name", playerContainer.name);
            player.put("handSize",  playerContainer.getNumCardsInHand());
            numCardsInHand.put(player);
        }

        JSONArray stack = new JSONArray();
        for(Card card : discardPile){
            JSONObject cardObj = new JSONObject();
            cardObj.put("ID",card.getID());
            cardObj.put("origin", card.getOrigin());
            stack.put(cardObj);
        }

        JSONObject serializedState = new JSONObject();
        serializedState.put("activePlayer", activePlayer.getName());
        serializedState.put("cardsToDraw", cardsToDraw);
        serializedState.put("numCardsInHand", numCardsInHand);
        serializedState.put("stack",stack);

        return serializedState;
    }

    public static GlobalState fromJSON(JSONObject jsonObject) throws  JSONException{
        GlobalState deserializedState = new GlobalState();
        deserializedState.activePlayer = new Player(jsonObject.getString("activePlayer"));
        deserializedState.cardsToDraw = jsonObject.getInt("cardsToDraw");
        JSONArray numCardsInHand = jsonObject.getJSONArray("numCardsInHand");
        JSONArray stack = jsonObject.getJSONArray("stack");

        deserializedState.players = new ArrayList<>();
        for (int i = 0; i < numCardsInHand.length(); i++){
            JSONObject player = numCardsInHand.getJSONObject(i);
            PlayerContainer container = new PlayerContainer(player.getString("name"), player.getInt("handSize"));
            deserializedState.players.add(container);
        }

        deserializedState.discardPile = new ArrayList<>();
        for (int i = 0; i < stack.length(); i++){
            JSONObject cardObject = stack.optJSONObject(i);

            JSONObject playParams = cardObject.optJSONObject("playParameters");
            Card card = EinzSingleton.getInstance().getCardLoader().getCardInstance(
                            cardObject.getString("ID"),
                            cardObject.getString("origin"), playParams);
            deserializedState.discardPile.add(card);
        }

        return deserializedState;
    }

    /**
     * Private Container class to handle the missing hand on client-side
     */
    private static class PlayerContainer {
        private int numCardsInHand;
        final String name;
        final Player player;

        PlayerContainer(Player player){
            this.name = player.getName();
            this.player = player;
        }

        PlayerContainer(String name, int numCardsInHand){
            this.name = name;
            this.player = null;
            this.numCardsInHand = numCardsInHand;
        }

        int getNumCardsInHand(){
            if(player != null){
                numCardsInHand = player.hand.size();
            }
            return numCardsInHand;
        }
    }
}
