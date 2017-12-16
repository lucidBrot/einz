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

    private HashMap<String, Integer> points; // String for player.getName(),  String for points

    private JSONObject lastPlayParameters;

    /**
     * Creates an instance of a Global game state.
     *
     * @param maxDiscardPileSize Sets the maximum size of the Discard Pile
     * @param orderOfPlayers     An ordered list of players determining the order in which the players will play.
     */
    public GlobalState(int maxDiscardPileSize, List<Player> orderOfPlayers) {
        if (Debug.smallStack >= 0) {
            Log.w("DEBUG", "Using a small stack max size of " + Debug.smallStack + ". To disable, set Debug.smallStack to -1");
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
        this.points = new HashMap<>();
        this.lastPlayParameters = new JSONObject();
    }

    /**
     * Creates an instance of a Global game state initialized with an initial card. The Order of the
     * players also has to be declared at the beginning of the game.
     *
     * @param maxDiscardPileSize Sets the maximum size of the Discard Pile
     * @param startCard          initializes the gameState with a start card on the discard pile
     * @param orderOfPlayers     An ordered list of players determining the order in which the players will play.
     */
    public GlobalState(int maxDiscardPileSize, List<Player> orderOfPlayers, Card startCard) {
        this(maxDiscardPileSize, orderOfPlayers);
        addCardToDiscardPile(startCard);

    }

    private GlobalState() {
        maxDiscardPileSize = 0;
    }

    /**
     * Adds the (possibly negative) points to the score of the player identified by his name.
     * If there were no points previously, it will set them to the new value
     */
    public void addPoints(String playerName, Integer pointsToAdd) {
        if (points.containsKey(playerName)) {
            points.put(playerName, points.get(playerName) + pointsToAdd);
        } else {
            points.put(playerName, pointsToAdd);
        }
    }

    /**
     * Returns the current Points for a Player. If the given Player does not have any Points set
     * null is returned
     *
     * @param playerName The Name of the player to get the points for
     * @return Number of points or null if the player could not be found
     */
    public Integer getPointsForPlayer(String playerName) {
        if (!points.containsKey(playerName))
            return null;
        return points.get(playerName);
    }

    public HashMap<String, Integer> getPointMapping() {
        return new HashMap<>(points);
    }

    /**
     * sets the points for the player if it exists, otherwise creates this player
     */
    public void setPointsForPlayer(String playerName, Integer points) {
        this.points.put(playerName, points);
    }

    /**
     * Returns a reference to the discardPile. Rules may change it. The lowermost Card is at index 0
     * and the top card is at the end of the list.
     *
     * @return Reference to the Discardpile
     */
    public List<Card> getDiscardPile() {
        return discardPile;
    }

    /**
     * Returns the top of the discardPile.
     *
     * @return Card on top of the discardPile
     */
    public Card getTopCardDiscardPile() {
        return discardPile.get(discardPile.size() - 1);
    }

    /**
     * Adds many cards to the discard pile. the Cards have to be ordered such that the card that
     * should be on the lowermost position is at index 0. the discard Pile gets trimmed to the
     * macDiscardPileSize.
     *
     * @param cards List of cards to add to the discardPile
     */
    public void addCardsToDiscardPile(List<Card> cards) {
        discardPile.addAll(cards);
        discardPile.subList(Math.max(0, discardPile.size() - maxDiscardPileSize), discardPile.size());
    }

    /**
     * Adds one Card to the discard pile and sets it on top of the pile (at the end of the list)
     *
     * @param card Card to play on top of the discard pile
     */
    public void addCardToDiscardPile(Card card) {
        discardPile.add(card);
        if (discardPile.size() > maxDiscardPileSize) {
            discardPile = discardPile.subList(1, discardPile.size());
        }
    }

    /**
     * Adds the given List of Cards to the Draw pile to refill the draw Pile.
     *
     * @param cards Cards to add to the Draw Pile
     */
    public void addCardsToDrawPile(List<Card> cards) {
        drawPile.addAll(cards);
    }

    /**
     * Draws a given amount of cards from the drawPile. If there are not enough cards on the drawPile
     * null is returned. The client has to fill up the drawPile first before drawing the same amount
     * again. If numberOfCards is smaller than one an empty List is returned.
     *
     * @param numberOfCards Number of Cards to Draw. Has to be greater that zero.
     * @return List of the drawn Cards or null if there were not enough Cards in the drawPile for
     * this query.
     */
    public List<Card> drawCards(int numberOfCards) {
        if (numberOfCards < 1) {
            return new ArrayList<>();
        }

        if (drawPile.size() < numberOfCards) {
            return null;
        }

        List<Card> result = drawPile.subList(0, numberOfCards);
        drawPile = drawPile.subList(numberOfCards, drawPile.size());
        return result;
    }

    /**
     * Returns the amount that a player has to draw if he decides to draw cards.
     *
     * @return Amount of cards to draw.
     */
    public int getCardsToDraw() {
        return cardsToDraw;
    }

    /**
     * Sets the amount of cards a player has to draw if he decides to draw cards. The value has to
     * be non-negative.
     *
     * @param cardsToDraw Amount the next player should draw. Should be greater equal zero.
     */
    public void setCardsToDraw(int cardsToDraw) {
        if (cardsToDraw >= 0) {
            this.cardsToDraw = cardsToDraw;
        }
    }

    /**
     * @return a list of all players, both finished and unfinished
     */
    public List<Player> getAllPlayers(){
        List<Player> list = getPlayersOrdered();
        list.addAll(getFinishedPlayers());
        return list;
    }

    /**
     * Returns the ordered List of players. Rules are not meant to rearrange the list.
     *
     * @return A copy of the players list
     */
    public List<Player> getPlayersOrdered() {
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
     *
     * @return ordered map to map player name to numbers of cards
     */
    public LinkedHashMap<String, Integer> getPlayerHandSizeOrdered() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (PlayerContainer container : players) {
            result.put(container.name, container.getNumCardsInHand());
        }
        return result;
    }

    public Player getPlayer(String name){
        for(PlayerContainer container : players){
            if(container.name.equals(name)){
                return container.player;
            }
        }
        return null;
    }

    /**
     * Returns the Player who's turn it is currently
     *
     * @return the Player on turn
     */
    public Player getActivePlayer() {
        return activePlayer;
    }

    /**
     * Returns if the game should be ended. Is set to true if a rule decides to end the game.
     *
     * @return True if the game has finished.
     */
    public boolean isGameFinished() {
        return gameFinished;
    }

    /**
     * Sets the finished Flag to true.
     */
    public void finishGame() {
        gameFinished = true;
    }

    /**
     * Moves the player Object from the list of active Players to the list of finished players.
     *
     * @param player The Player to set finished
     */
    public void setPlayerFinished(Player player) {
        Log.d(Thread.currentThread().getName(), "setPlayerFinished(" + player.getName() + ")");
        for (int i = 0; i < players.size(); i++) {
            PlayerContainer pc = players.get(i);
            if (pc.player.getName().equals(player.getName())) {
                players.remove(pc);
                finishedPlayers.add(player);
                break;
            }
        }
        if (nextPlayer.getName().equals(player.getName())) {
            advanceNextPlayer();
        }
    }

    public List<Player> getFinishedPlayers() {
        return new ArrayList<>(finishedPlayers);
    }

    public void removePlayer(Player player) {
        Log.d(Thread.currentThread().getName(), "removePlayer(" + player.getName() + ")");
        for (int i = 0; i < players.size(); i++) {
            PlayerContainer container = players.get(i);
            if (container.player.getName().equals(player.getName())) {
                players.remove(container);
                break;
            }
        }
        if (nextPlayer.getName().equals(player.getName())) {
            advanceNextPlayer();
        }
    }

    /**
     * Sets the nextPlayer to the activePlayer and sets the new nextPlayer taking playOrderIsForward
     * into account.
     */
    public void nextTurn() {
        activePlayer = nextPlayer;
        advanceNextPlayer();
    }

    private void advanceNextPlayer() {
        int playerIndex = 0;
        for(int i=0; i < players.size(); i++){
            if(players.get(i).player.getName().equals(nextPlayer.getName())){
                playerIndex = i;
                break;
            }
        }
        if (players.size() > 0) {
            if (playOrderIsForwards) {
                nextPlayer = players.get((playerIndex + 1) % players.size()).player;
            } else {
                nextPlayer = players.get((playerIndex + players.size() - 1) % players.size()).player;
            }
        } // else whatever. TODO: set this to null or what should be done here?
    }

    public JSONObject getLastPlayParameters() {
        return lastPlayParameters;
    }

    public void setLastPlayParameters(JSONObject lastPlayParameters) {
        this.lastPlayParameters = lastPlayParameters;
    }

    /**
     * Serializes the GlobalState object to JSON.
     *
     * @return To JSON serialized GlobalState object
     */
    public JSONObject toJSON() throws JSONException {

        JSONArray numCardsInHand = new JSONArray();
        for (PlayerContainer playerContainer : players) {
            JSONObject player = new JSONObject();
            player.put("name", playerContainer.name);
            player.put("handSize", playerContainer.getNumCardsInHand());
            numCardsInHand.put(player);
        }

        JSONArray stack = new JSONArray();
        for (Card card : discardPile) {
            JSONObject cardObj = new JSONObject();
            cardObj.put("ID", card.getID());
            cardObj.put("origin", card.getOrigin());
            stack.put(cardObj);
        }

        JSONObject serializedState = new JSONObject();
        serializedState.put("activePlayer", activePlayer.getName());
        serializedState.put("cardsToDraw", cardsToDraw);
        serializedState.put("numCardsInHand", numCardsInHand);
        serializedState.put("stack", stack);
        serializedState.put("lastPlayParameters", lastPlayParameters);

        return serializedState;
    }

    public static GlobalState fromJSON(JSONObject jsonObject) throws JSONException {
        GlobalState deserializedState = new GlobalState();

        deserializedState.activePlayer = new Player(jsonObject.getString("activePlayer"));
        deserializedState.cardsToDraw = jsonObject.getInt("cardsToDraw");
        deserializedState.lastPlayParameters = jsonObject.getJSONObject("lastPlayParameters");

        JSONArray numCardsInHand = jsonObject.getJSONArray("numCardsInHand");
        JSONArray stack = jsonObject.getJSONArray("stack");

        deserializedState.players = new ArrayList<>();
        for (int i = 0; i < numCardsInHand.length(); i++) {
            JSONObject player = numCardsInHand.getJSONObject(i);
            PlayerContainer container = new PlayerContainer(player.getString("name"), player.getInt("handSize"));
            deserializedState.players.add(container);
        }

        deserializedState.discardPile = new ArrayList<>();
        for (int i = 0; i < stack.length(); i++) {
            JSONObject cardObject = stack.optJSONObject(i);

            Card card = EinzSingleton.getInstance().getCardLoader().getCardInstance(
                    cardObject.getString("ID"),
                    cardObject.getString("origin"));
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

        PlayerContainer(Player player) {
            this.name = player.getName();
            this.player = player;
        }

        PlayerContainer(String name, int numCardsInHand) {
            this.name = name;
            this.player = null;
            this.numCardsInHand = numCardsInHand;
        }

        int getNumCardsInHand() {
            if (player != null) {
                numCardsInHand = player.hand.size();
            }
            return numCardsInHand;
        }
    }
}
