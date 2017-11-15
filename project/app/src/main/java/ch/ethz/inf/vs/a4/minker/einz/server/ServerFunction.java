package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.CardAttributeList;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardColors;
import ch.ethz.inf.vs.a4.minker.einz.CardTypes;
import ch.ethz.inf.vs.a4.minker.einz.GameState;
import ch.ethz.inf.vs.a4.minker.einz.Player;

/**
 * Created by Fabian on 09.11.2017.
 */

public class ServerFunction implements ServerFunctionDefinition {

    //reference to the server thread containing the global state object
    public ThreadedEinzServer server;

    //Constructor
    public ServerFunction(ThreadedEinzServer TES){
        server = TES;
    }
    public ServerFunction(){
        //Does not initialise server!
    }



    //(I) Functions to check after the state of the game (no side effects)

    //look at the top card of the playpile on the table
    public Card topCard(){
        return server.gameState.playPiletopCard();
    };

    //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
    //returns the full pile of played cards otherwise
    public ArrayList<Card> playedCards (int x){
        return server.gameState.playedCards(x);
    };

    //returns the player whos turn it currently is
    public Player activePlayer(){
        return server.gameState.getActivePlayer();
    };

    //checks if a card can be played
    public boolean isPlayable (Card card, Player p){
        //Here come all the fancy rules!
        //So far, only the very basics are implemented
        boolean pTurn = false, cardLegit = false;
        Card bottomCard = topCard();

        if (server.gameState.getThreatenedCards() == 1) {
            //Thesse are the regular rules
            cardLegit = normalPlayRules(bottomCard, card);
        } else {
            //These are the rules when an active plusTwo or changeColorPlusFour is in the middle
            cardLegit = specialPlayRules(bottomCard, card);
        }

        if (activePlayer().equals(p)) {
            pTurn = true;
        }
        if (cardLegit && pTurn) {
            return true;
        } else {
            return false;
        }

    }

    //returns the number of cards a player needs to draw if he can't play anything
    public int cardsToDraw(){
        return server.gameState.getThreatenedCards();
    }

    //returns if the active player has already drawn his one card because he was not able to play anything
    //(doesn't include if the player has drawn cards from card effects)
    public boolean hasDrawn(){
        return server.gameState.getHasDrawn();
    }

    //(II) Functions that change things in the game

    //initialises a new game
    //returns a new GameState object with:
    //-the given players in the game, the players play in the order in which they are in the ArrayList (lowest index plays first)
    //-a deck that contains the specified cards the specified amount of times
    // in the HashMap, the Key determines the Card and the Mapped value determines how many times that card is put into the game
    //-the given set of rules with which the game is played represented as an int
    // since we haven't specified yet what options should be available, this int does nothing at the moment
    public GameState startGame(ArrayList<Player> players, HashMap<Card, Integer> deck, int rules){
        return new GameState(players, deck, rules);
    }

    public GameState startStandartGame(ArrayList<Player> players){
        return new GameState(players);
    }

    //playing a card
    public boolean play(Card card, Player p){
        if (isPlayable(card, p)){
            server.gameState.playCardFromHand(card, p);
            cardEffect(card);
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
        server.gameState.setHasDrawn(true);
        return server.gameState.drawOneCard(p);
    }

    //returns the top x cards from the drawpile to be drawn and ramoves them from the drawpile and adds them to player p's hand
    //this should be called when a player needs to draw cards because of effects from played cards
    public HashSet<Card> drawXCards(int x, Player p){
        HashSet result = new HashSet();
        for (int i = 0; i < x; i++){
            result.add(server.gameState.drawOneCard(p));
        }
        server.gameState.resetThreatenedCards();
        return result;
    }

    // (III) Other Functions

    //checks if a player has won the game (has 0 cards in hand)
    public boolean hasWon(Player p){
        if (p.Hand.isEmpty()){
            return true;
        } else {
            return false;
        }
    }


    //Ends the running game
    public void endGame(){
        //TODO: Implement
        //Maybe this isn't needed since the ServerFunction only keeps the GameState in a valid state but once the game ends
        //this isn't necessary anymore
    }

    public void cardEffect(Card card){
        switch (card.type){
            case PLUSTWO:
                if (server.gameState.getThreatenedCards() == 1){
                    server.gameState.increaseThreatenedCards(1);
                } else {
                    server.gameState.increaseThreatenedCards(2);
                }
                break;
            case CHANGECOLORPLUSFOUR:
                if (server.gameState.getThreatenedCards() == 1){
                    server.gameState.increaseThreatenedCards(3);
                } else {
                    server.gameState.increaseThreatenedCards(4);
                }
                break;
            case STOP:
                server.gameState.nextPlayer();
                break;
            case SWITCHORDER:
                server.gameState.switchOrder();
                break;
        }
        server.gameState.nextPlayer();
    }

    public boolean normalPlayRules(Card bottomCard, Card topCard){
        switch (topCard.type){
            case CHANGECOLOR:
            case CHANGECOLORPLUSFOUR:
                return true;
            default:
                if (topCard.color == bottomCard.color ||
                        topCard.type == bottomCard.type ||
                        topCard.color == bottomCard.wish){
                    return true;
                } else {
                    return false;
                }
        }
    }

    public boolean specialPlayRules(Card bottomCard, Card topCard){
        switch (topCard.type){
            case PLUSTWO:
                if (bottomCard.type == CardTypes.PLUSTWO){
                    return true;
                } else {
                    return false;
                }
            case CHANGECOLORPLUSFOUR:
                if (bottomCard.type == CardTypes.CHANGECOLORPLUSFOUR){
                    return true;
                } else {
                    return false;
                }
            default:
                return false;
        }
    }

}
