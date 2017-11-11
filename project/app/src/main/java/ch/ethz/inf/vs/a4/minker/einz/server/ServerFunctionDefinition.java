package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Fabian on 09.11.2017.
 */

public interface ServerFunctionDefinition {

    /*
    These Functions don't have an effect on EinzServerClientHandlers or Clients
    They only act on the server.gamestate object to keep it in a valid state and represent actions on that state
    e.g. playing a card)
    */

    //(I) Functions to check after the state of the game (no side effects)


        //There is no ping function. EinzServerClientHandler respond with Pong themselves.

        //look at the top card of the open cardpile on the table
        public Card topCard();

        //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
        //returns the full pile of played cards otherwise
        //keys in HashMap indicate how many cards lie on top of the card (top card has key 0)
        public HashMap<Integer, Card> playedCards (int x);

        //returns the player whose turn it currently is
        public Player activePlayer();

        //check if player p can play a certain card
        public boolean isPlayable (Card card, Player p);

    //(II) Functions that change things in the game

        //player p plays a card
        //This automatically sets the activePlayer to the next player to play after p laid his card
        public void play(Card card, Player p);

        //returns the top card from the drawpile to be drawn and removes it from the drawpile and adds it top player p's hand
        //this should be called when it's player p's turn and he can't play any cards from hand
        public Card drawOneCard(Player p);

        //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile and adds them to player p's hand
        //this should be called when player p needs to draw cards because of effects from played cards
        public HashSet<Card> drawXCards(int x, Player p);

    // (III) Other Functions

        //checks if a player has won the game (has 0 cards in hand)
        public boolean hasWon(Player p);



}
