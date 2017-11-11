package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.HashMap;
import java.util.HashSet;

import ch.ethz.inf.vs.a4.minker.einz.CardAttributeList;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.Player;

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

    //look at the top card of the playpile on the table
    public Card topCard(){
        return server.gameState.topCard();
    };

    //returns the top x cards from the pile of played cards if at least x cards lie on the pile of played cards
    //returns the full pile of played cards otherwise
    public HashMap<Integer, Card> playedCards (int x){
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
            switch (card.type) {
                //looks rather tedious to do it this way, but adding additional rules for cards is very easy with this structure
                case CardAttributeList.zero:
                    if (bottomCard.type.equals(CardAttributeList.zero) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.one:
                    if (bottomCard.type.equals(CardAttributeList.one) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.two:
                    if (bottomCard.type.equals(CardAttributeList.two) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.three:
                    if (bottomCard.type.equals(CardAttributeList.three) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.four:
                    if (bottomCard.type.equals(CardAttributeList.four) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.five:
                    if (bottomCard.type.equals(CardAttributeList.five) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.six:
                    if (bottomCard.type.equals(CardAttributeList.six) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.seven:
                    if (bottomCard.type.equals(CardAttributeList.seven) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.eight:
                    if (bottomCard.type.equals(CardAttributeList.eight) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.nine:
                    if (bottomCard.type.equals(CardAttributeList.nine) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.plusTwo:
                    if (bottomCard.type.equals(CardAttributeList.plusTwo) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.switchOrder:
                    if (bottomCard.type.equals(CardAttributeList.switchOrder) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.stop:
                    if (bottomCard.type.equals(CardAttributeList.stop) || card.color.equals(bottomCard.color) || card.color.equals(bottomCard.wish)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.changeColor:
                    cardLegit = true;
                    break;
                case CardAttributeList.changeColorPlusFour:
                    cardLegit = true;
                    break;
                default:
                    cardLegit = false;
            }

            if (activePlayer().equals(p)) {
                pTurn = true;
            }

            if (cardLegit && pTurn) {
                return true;
            } else {
                return false;
            }

        } else {
            //These are the rules when an active plusTwo or changeColorPlusFour is in the middle
            switch (card.type) {
                //looks rather tedious to do it this way, but adding additional rules for cards is very easy with this structure
                case CardAttributeList.zero:
                    cardLegit = false;
                    break;
                case CardAttributeList.one:
                    cardLegit = false;
                    break;
                case CardAttributeList.two:
                    cardLegit = false;
                    break;
                case CardAttributeList.three:
                    cardLegit = false;
                    break;
                case CardAttributeList.four:
                    cardLegit = false;
                    break;
                case CardAttributeList.five:
                    cardLegit = false;
                    break;
                case CardAttributeList.six:
                    cardLegit = false;
                    break;
                case CardAttributeList.seven:
                    cardLegit = false;
                    break;
                case CardAttributeList.eight:
                    cardLegit = false;
                    break;
                case CardAttributeList.nine:
                    cardLegit = false;
                    break;
                case CardAttributeList.plusTwo:
                    if (bottomCard.type.equals(CardAttributeList.plusTwo)) {
                        cardLegit = true;
                    }
                    break;
                case CardAttributeList.switchOrder:
                    cardLegit = false;
                    break;
                case CardAttributeList.stop:
                    cardLegit = false;
                    break;
                case CardAttributeList.changeColor:
                    cardLegit = false;
                    break;
                case CardAttributeList.changeColorPlusFour:
                    cardLegit = true;
                    break;
                default:
                    cardLegit = false;
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
    }

    //(II) Functions that change things in the game

    //playing a card
    public void play(Card card, Player p){
        //Here comes all the fancy stuff that happens when a card is played!
        //So far, only the basics are implemented
        if (isPlayable(card, p)){
            server.gameState.playCardFromHand(card, p);
            switch (card.type) {
                //looks rather tedious to do it this way, but adding additional rules for cards is very easy with this structure
                //Here come the additional effects when a card is played
                case CardAttributeList.zero:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.one:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.two:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.three:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.four:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.five:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.six:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.seven:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.eight:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.nine:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.plusTwo:
                    if (server.gameState.getThreatenedCards() == 1){
                        server.gameState.increaseThreatenedCards(1);
                    } else {
                        server.gameState.increaseThreatenedCards(2);
                    }
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.switchOrder:
                    server.gameState.switchOrder();
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.stop:
                    server.gameState.nextPlayer();
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.changeColor:
                    server.gameState.nextPlayer();
                    break;
                case CardAttributeList.changeColorPlusFour:
                    if (server.gameState.getThreatenedCards() == 1){
                        server.gameState.increaseThreatenedCards(3);
                    } else {
                        server.gameState.increaseThreatenedCards(4);
                    }
                    server.gameState.nextPlayer();
                    break;
                default:
                    break;
            }

        }

    }

    //returns the top card from the drawpile to be drawn and removes it from the drawpile and adds it to player p's hand
    //this should be called when it's a players turn and he can't play any cards from hand
    public Card drawOneCard(Player p){
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
}
