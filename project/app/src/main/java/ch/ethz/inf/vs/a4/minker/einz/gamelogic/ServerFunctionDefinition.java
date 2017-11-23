package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardColors;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Spectator;

/**
 * Created by Fabian on 09.11.2017.
 */

public interface ServerFunctionDefinition {

    /*
    These Functions don't have an effect on EinzServerClientHandlers or Clients
    They only act on the server.gamestate object to keep it in a valid state and represent actions in the game
    e.g. playing a card)
    */

    //(I) Functions to check after the state of the game (no side effects)


        //There is no ping function. EinzServerClientHandler respond with Pong themselves.

        //look at the top card of the open cardpile on the table
        public Card topCard();

        //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
        //returns the full pile of played cards otherwise
        //index indicates how many cards lie below the card with that index
        public ArrayList<Card> playedCards (int x);

        //returns the player whose turn it currently is
        public Player activePlayer();

        //check if player p can play a certain card
        public boolean isPlayable (Card card, Player p);

        //returns the number of cards a player needs to draw if he can't play anything
        public int cardsToDraw();

        //returns if the active player has already drawn his one card because he was not able to play anything
        //(doesn't include if the player has drawn cards from card effects)
        public boolean hasDrawn();

        //checks if a player has won the game (has 0 cards in hand)
        public boolean hasWon(Player p);

    //(II) Functions that change things in the game

        //initialises a new game
        //returns a new GameState object with:
        //-the given players in the game, the players play in the order in which they are in the ArrayList (lowest index plays first)
        //-a deck that contains the specified cards the specified amount of times
        // in the HashMap, the Key determines the Card and the Mapped value determines how many times that card is put into the game
        //-the given set of rules with which the game is played represented as an int
        // since we haven't specified yet what options should be available, this int does nothing at the moment
        public GameState initialiseGame(ArrayList<Player> players, HashMap<Card, Integer> deck, int rules);

        //initialises a new game with standart cards and rules with the given players and spectators
        //if there are no spectators, just use an empty hashset as input (or NULL)
        public GameState initialiseStandartGame(ArrayList<Player> players, HashSet<Spectator> spectators);

        //sends all players the message that the game started
        //sends all players the relevant information they need to have (defined in GlobalState and PlayerState)
        public void startGame();

        //player p plays a card
        //This automatically sets the activePlayer to the next player to play after p laid his card
        //returns whether the card is playable (and therefore played) or not
        public boolean play(Card card, Player p);

        //player  p plays a card with the given wish
        //for a changeColor or changeColorPlusFour wish has to be red, yellow, blue or green
        //returns whether the card is playable (and therefore played) or not
        public boolean play(Card card, Player p, CardColors wish);

        //returns the top card from the drawpile to be drawn and removes it from the drawpile and adds it to player p's hand
        //this should be called when it's player p's turn to play and he can't play any cards from hand
        //if a player needs to draw cards from card effects, use drawXCards instead
        public Card drawOneCard(Player p);

        //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile and adds them to player p's hand
        //this should be called when player p needs to draw cards because of effects from played cards
        public ArrayList<Card> drawXCards(int x, Player p);

        //Ends the running game
        //currently does nothing since the GameState shouldn't care what happens after the game?
        public void endGame();

        //removes a player from the game and coninues the game without that player
        public void removePlayer(Player p);






}
