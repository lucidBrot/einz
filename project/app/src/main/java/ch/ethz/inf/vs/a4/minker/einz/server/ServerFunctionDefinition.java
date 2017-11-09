package ch.ethz.inf.vs.a4.minker.einz.server;

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


    //(I) Functions to check after the state of the game (no side effects)
        //simple ping function
        public void ping();

        //look at the top card of the open cardpile on the table
        public ICardDefinition topCard();

        //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
        //returns the full pile of played cards otherwise
        public HashSet<ICardDefinition> playedCards (int x);

        //returns the player whos turn it currently is
        public PlayerDefinition activePlayer();

        //check if a card can be played
        public boolean isPlayable (ICardDefinition card, PlayerDefinition p);

    //(II) Functions that change things in the game

        //playing a card
        public void play(ICardDefinition card, PlayerDefinition p);

        //returns the top card from the drawpile to be drawn and removes it from the drawpile
        //this should be called when it's a players turn and he can't play any cards from hand
        public ICardDefinition drawOneCard(PlayerDefinition p);

        //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile
        //this should be called when a player needs to draw cards because of effects from played cards
        public HashSet<ICardDefinition> drawXCards(int x);

    // (III) Other Functions

        //checks if a player has won the game (has 0 cards in hand)
        public boolean hasWon(PlayerDefinition p);



}
