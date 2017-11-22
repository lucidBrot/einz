package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.*;

/**
 * Created by Fabian on 09.11.2017.
 */

public class ServerFunction implements ServerFunctionDefinition {

    //reference to the the global state object
    public GameState gameState;

    //reference to the ThreadedEinzServer
    public ThreadedEinzServer threadedEinzServer;

    //Constructor
    public ServerFunction(GameState gs, ThreadedEinzServer tes){
        gameState = gs;
        threadedEinzServer = tes;
    }
    public ServerFunction(GameState gs){
        //Does not initialise server!
        gameState = gs;
    }
    public ServerFunction(){
        //doesn't initialise anything!
        //no guarantees that it works if you call this
    }



    //(I) Functions to check after the state of the game (no side effects)

    //look at the top card of the playpile on the table
    public Card topCard(){
        return gameState.playPiletopCard();
    };

    //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
    //returns the full pile of played cards otherwise
    public ArrayList<Card> playedCards (int x){
        return gameState.playedCards(x);
    };

    //returns the player whos turn it currently is
    public Player activePlayer(){
        return gameState.getActivePlayer();
    };

    //checks if a card can be played
    public boolean isPlayable (Card card, Player p){
        return gameState.isPlayable(card, p);
    }

    //returns the number of cards a player needs to draw if he can't play anything
    public int cardsToDraw(){
        return gameState.getThreatenedCards();
    }

    //returns if the active player has already drawn his one card because he was not able to play anything
    //(doesn't include if the player has drawn cards from card effects)
    public boolean hasDrawn(){
        return gameState.getHasDrawn();
    }

    //checks if a player has won the game (has 0 cards in hand)
    public boolean hasWon(Player p){
        if (p.hand.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    //(II) Functions that change things in the game

    //initialises a new game
    //returns a new GameState object with:
    //-the given players in the game, the players play in the order in which they are in the ArrayList (lowest index plays first)
    //-a deck that contains the specified cards the specified amount of times
    // in the HashMap, the Key determines the Card and the Mapped value determines how many times that card is put into the game
    //-the given set of rules with which the game is played represented as an int
    // since we haven't specified yet what options should be available, this int does nothing at the moment
    public GameState initialiseGame(ArrayList<Player> players, HashMap<Card, Integer> deck, int rules){
        return new GameState(players, deck, rules);
    }

    public GameState initialiseStandartGame(ArrayList<Player> players){
        return new GameState(players);
    }

    @Override
    public GameState initialiseStandardGame(ArrayList<Player> players, ArrayList<Spectator> spectators) {
        return initialiseStandartGame(players); // TODO: implement. Assume the caller will not keep the GameState
    }


    //sends all players the message that the game started
    //sends all players the relevant information they need to have (defined in GlobalInfo and PlayerInfo)
    public void startGame(){
        //TODO: Implement

    }

    //playing a card
    public boolean play(Card card, Player p){
        if (isPlayable(card, p)){
            gameState.playCardFromHand(card, p);
            gameState.cardEffect(card);
            return true;
        } else {
            return false;
        }

    }
    //When a player plays a card that has a wish he can make, call this instead of play(card, player).
    public boolean play(Card card, Player p, CardColors wish){
        if (wish.equals(CardColors.NONE)){
            return false;
        } else {
            card.wish = wish;
            return play(card, p);
        }
    }

    //returns the top card from the drawpile to be drawn and removes it from the drawpile and adds it to player p's hand
    //this should be called when it's a players turn and he can't play any cards from hand
    public Card drawOneCard(Player p){
        gameState.setHasDrawn(true);
        return gameState.drawOneCard(p);
    }

    //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile and adds them to player p's hand
    //this should be called when a player needs to draw cards because of effects from played cards
    public ArrayList<Card> drawXCards(int x, Player p){
        ArrayList result = new ArrayList();
        for (int i = 0; i < x; i++){
            result.add(gameState.drawOneCard(p));
        }
        gameState.resetThreatenedCards();
        return result;
    }

    //Ends the running game
    public void endGame(){
        //TODO: Create list of all players that were there at the start of the game with their ranking
    }

    //removes a player from the game and continues the game without that player
    public void removePlayer(String name){
        for (Player p: gameState.getPlayers()){
            if (p.name.equals((name))){
                if (gameState.getActivePlayer().equals(p)){
                    gameState.nextPlayer();
                }
                gameState.removePlayer(name);
                if (gameState.getPlayers().size() < 2){
                    endGame();
                }
            }
        }
    }
}
