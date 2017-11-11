package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.ICardDefinition;
import ch.ethz.inf.vs.a4.minker.einz.PlayerDefinition;

/**
 * Created by Fabian on 09.11.2017.
 */

public interface ServerFunctionDefinition {

    //reference to the server thread containing the global state object
    //gets set in constructor
    public ThreadedEinzServer Server = null;

    /*
    These Functions don't have an effect on EinzServerClientHandlers or Clients.
    They only act on the server.gamestate object to keep it in a valid state and represent actions on that state
    (e.g. playing a card) Everything done in here has to be communicated seperatly to the Clients
    */

    //(I) Functions to check after the state of the game (no side effects)

    /**
     * Answers the message with "pong"
     */

        //There is no ping function. EinzServerClientHandler respond with Pong themselves.

        //look at the top card of the open cardpile on the table
        public ICardDefinition topCard();

        //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
        //returns the full pile of played cards otherwise
        //keys in HashMap indicate how many cards lie on top of the card (top card has key 0)
        public HashMap<Integer, ICardDefinition> playedCards (int x);

        //returns the player whose turn it currently is
        public PlayerDefinition activePlayer();

        //check if player p can play a certain card
        public boolean isPlayable (ICardDefinition card, PlayerDefinition p);

    //(II) Functions that change things in the game

        //player p plays a card
        //This automatically sets the activePlayer to the next player to play after p laid his card
        public void play(ICardDefinition card, PlayerDefinition p);

        //returns the top card from the drawpile to be drawn and removes it from the drawpile and adds it top player p's hand
        //this should be called when it's player p's turn and he can't play any cards from hand
        public ICardDefinition drawOneCard(PlayerDefinition p);

        //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile and adds them to player p's hand
        //this should be called when player p needs to draw cards because of effects from played cards
        public HashSet<ICardDefinition> drawXCards(int x, PlayerDefinition p);

    // (III) Other Functions

        //checks if a player has won the game (has 0 cards in hand)
        public boolean hasWon(PlayerDefinition p);



}
