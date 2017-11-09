package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;
import ch.ethz.inf.vs.a4.minker.einz.PlayerDefinition;

/**
 * Created by Fabian on 09.11.2017.
 */

public class ServerFunction implements ServerFunctionDefinition {

    //Constructor
    public void ServerFunction(ThreadedEinzServer TES){
        server = TES;
    }
    //reference to the server thread containing the global state object
    //gets set in constructor
    public ThreadedEinzServer server;


    //(I) Functions to check after the state of the game (no side effects)
    //simple ping function
    public void ping(){

    };

    //look at the top card of the playpile on the table
    public ICardDefinition topCard(){
        return server.gameState.topCard();
    };

    //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
    //returns the full pile of played cards otherwise
    public HashMap<Integer, ICardDefinition> playedCards (int x){
        return server.gameState.playedCards(x);
    };

    //returns the player whos turn it currently is
    public PlayerDefinition activePlayer(){
        return server.gameState.getActivePlayer();
    };

    //check if a card can be played
    public boolean isPlayable (ICardDefinition card, PlayerDefinition p){
        return server.gameState.isPlayable(card, p);
    };

    //(II) Functions that change things in the game

    //playing a card
    public void play(ICardDefinition card, PlayerDefinition p){

    };

    //returns the top card from the drawpile to be drawn and removes it from the drawpile
    //this should be called when it's a players turn and he can't play any cards from hand
    public ICardDefinition drawOneCard(PlayerDefinition p){
        return null;
    };

    //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile
    //this should be called when a player needs to draw cards because of effects from played cards
    public HashSet<ICardDefinition> drawXCards(int x, PlayerDefinition p){
        return null;
    };

    // (III) Other Functions

    //checks if a player has won the game (has 0 cards in hand)
    public boolean hasWon(PlayerDefinition p){
        return false;
    };
}
