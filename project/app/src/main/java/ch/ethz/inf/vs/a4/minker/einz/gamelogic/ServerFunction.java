package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import ch.ethz.inf.vs.a4.minker.einz.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.CardText;
import ch.ethz.inf.vs.a4.minker.einz.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.Player;
import ch.ethz.inf.vs.a4.minker.einz.Spectator;
import ch.ethz.inf.vs.a4.minker.einz.rules.ChangeDirectionRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.ResetCardsToDrawRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.StartGameWithCardsRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.WinOnNoCardsRule;

/**
 * Created by Fabian on 23.11.2017.
 */

public class ServerFunction implements ServerFunctionDefinition {

    private GlobalState globalState;
    private GameConfig gameConfig;
    private final static int MAX_NUMBER_OF_PLAYERS = 20;

    /**
     * initialises a new game with standard cards and rules
     *
     * @param players the players in the game, the players play in the order in which they are in the
     *                ArrayList (lowest index plays first)
     */

    public void initialiseStandardGame(ArrayList<Player> players) {
        if (players.size() < 2 || players.size() > MAX_NUMBER_OF_PLAYERS) {
            //don't initialise game
        } else {
            globalState = new GlobalState(10, players); // #cardtag
            this.gameConfig = createStandardConfig(players); //Create new standard GameConfig
            globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile()); //Set the drawPile of the GlobalState
            globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card
            globalState.nextPlayer = players.get(0); //There currently is no active player, nextplayer will start the game in startGame
        }
    }

    public void initialiseStandardGame(ArrayList<Player> players, HashSet<Spectator> spectators) {
        // Wieso eine arraylist und ein hashset?
        // Weil wir die Spieler in geordneter Reihenfolge brauchen, die Zuschauer aber nicht.
    }


    /**
     * initialises a new game
     *
     * @param players     the players in the game, the players play in the order in which they are in the
     *                    ArrayList (lowest index plays first)
     * @param deck        contains the specified cards the specified amount of times
     *                    in the HashMap, the Key determines the Card and the Mapped value determines how many times
     *                    that card is put into the game
     * @param globalRules set of global rules with which the game is played
     * @param cardRules   card rules with the card they should apply to
     */

    public void initialiseGame(ArrayList<Player> players, HashMap<Card, Integer> deck, Collection<BasicGlobalRule> globalRules,
                               Map<BasicCardRule, Card> cardRules) {
        if (players.size() < 2 || players.size() > MAX_NUMBER_OF_PLAYERS) {
            //don't initialise game
        } else {
            gameConfig = new GameConfig(deck);
            gameConfig.allCardsInGame.addAll(deck.keySet());
            for (Player p : players) {
                gameConfig.addParticipant(p);
            }
            for (BasicGlobalRule r : globalRules) {
                gameConfig.addGlobalRule(r);
            }
            for (BasicCardRule r : cardRules.keySet()) {
                gameConfig.assignRuleToCard(r, cardRules.get(r));
            }
        }
    }


    /**
     * Gives the correct amount of cards to each player
     * Sets the active player to the first player to play
     * Lets the players start playing
     */
    public void startGame() {
        GlobalRuleChecker.checkOnStartGame(globalState, gameConfig);
        globalState.nextTurn(); //Sets the active player to the one specified in initialiseGame
    }

    /**
     * player p wants to play a card, his card is only played if the rules allow him to.
     * OnPlayRules get applied after the player plays his card
     *
     * @param card the card to be played
     * @param p    the player that wants to playe a card
     * @return whether the player is allowed to play the card he wants to play or not
     */
    public boolean play(Card card, Player p) {
        if (!globalState.getActivePlayer().equals(p)) {
            return false; //TODO: Check in rules whether its a players turn
        }
        if (CardRuleChecker.checkIsValidPlayCard(globalState, card, gameConfig)) {
            p.hand.remove(card);
            globalState.addCardToDiscardPile(card);
            CardRuleChecker.checkOnPlayAssignedCard(globalState, card, gameConfig);
            CardRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
            return true;
        } else {
            return false;
        }
    }

    /**
     * OnDrawCard Rules get applied after the player draws cards
     * @param p the player that wants to draw cards
     * @return the Cards that player draws, if he is not allowed to draw cards returns null.
     */
    public ArrayList<Card> drawCards(Player p) {
        if (!globalState.getActivePlayer().equals(p)) {
            return null; //TODO: Check in rules whether its a players turn
        }
        if (CardRuleChecker.checkIsValidDrawCards(globalState, gameConfig)){
            ArrayList<Card> result = (ArrayList) globalState.drawCards(globalState.getCardsToDraw());
            p.hand.addAll(result);
            CardRuleChecker.checkOnDrawCard(globalState, gameConfig);
            return result;
        } else {
            return null;
        }
    }

    /**
     * ends the running game
     * not sure what this does exactly
     */
    public void endGame() {

    }

    /**
     * removes a Player from the game
     * If there are less than two players left after removing the Player, the game is ended automatically.
     *
     * @param player the player to be removed
     */
    public void removePlayer(Player player) {

        //TODO: save removed players in a List to create leaderboard at endGame()

    }

    /**
     * This function is not listed in the interface and is just used inside this class
     *
     * @return a new GameConfig with a standard deck consisting of 112 cards and the standard rules
     */
    private GameConfig createStandardConfig(List<Player> players) {
        Map<Card, Integer> numberOfCardsInGame = new HashMap<>();
        Set<Card> allCardsInGame = new HashSet<>();
        for (CardText ct : CardText.values()) {
            if (ct != CardText.CHANGECOLOR && ct != CardText.CHANGECOLORPLUSFOUR) {
                for (CardColor cc : CardColor.values()) {
                    if (cc != CardColor.NONE) {
                        Card card = new Card("temp", ct.type, ct, cc); // #cardtag replace "temp"
                        numberOfCardsInGame.put(card, 2);
                        allCardsInGame.add(card);
                    }
                }
            } else {
                Card card = new Card("temp", ct.type, ct, CardColor.NONE); // #cardtag
                numberOfCardsInGame.put(card, 4);
                allCardsInGame.add(card);
            }
        }
        GameConfig result = new GameConfig(numberOfCardsInGame);
        result.allCardsInGame = allCardsInGame;
        for (Player p : players) {
            result.addParticipant(p);
        }
        //Add all necessary GlobalRules: (StartGameWithCardsRule, WinOnNoCardsRule)
        result.addGlobalRule(new StartGameWithCardsRule());
        result.addGlobalRule(new WinOnNoCardsRule());
        result.addGlobalRule(new ResetCardsToDrawRule());

        //Add all necessary CardRules: (ChangeDirectionRule)
        for (CardColor cc : CardColor.values()) {
            result.assignRuleToCard(new ChangeDirectionRule(), new Card("temp", CardText.SWITCHORDER.type, CardText.SWITCHORDER, cc));
        }
        return result;
    }

}
