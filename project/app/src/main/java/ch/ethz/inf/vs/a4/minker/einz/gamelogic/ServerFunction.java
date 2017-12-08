package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;
import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.ChangeDirectionRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.NextTurnRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.ResetCardsToDrawRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.StartGameWithCardsRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.WinOnNoCardsRule;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;

/**
 * Created by Fabian on 23.11.2017.
 */

public class ServerFunction implements ServerFunctionDefinition {

    private ThreadedEinzServer threadedEinzServer;
    private GlobalState globalState;
    private GameConfig gameConfig;
    private final int MAX_NUMBER_OF_PLAYERS;

    /**
     * Doesn't initalise the threadedEinzServer!
     * Only used for debugging
     * Use one of the other constructors
     */
    public ServerFunction(){
        this.MAX_NUMBER_OF_PLAYERS = 20;
    }

    /**
     * @param threadedEinzServer the ThreadedEinzServer that holds the list of players and spectators
     *                           to send messages to during the game
     */
    public ServerFunction(ThreadedEinzServer threadedEinzServer) {
        this.threadedEinzServer = threadedEinzServer;
        this.MAX_NUMBER_OF_PLAYERS = 20;
    }

    /**
     * @param threadedEinzServer the ThreadedEinzServer that holds the list of players and spectators
     *                           to send messages to during the game
     * @param maxNumberOfPlayers the maximum number of Players allowed in a game
     */
    public ServerFunction(ThreadedEinzServer threadedEinzServer, int maxNumberOfPlayers) {
        this.threadedEinzServer = threadedEinzServer;
        this.MAX_NUMBER_OF_PLAYERS = maxNumberOfPlayers;
    }

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
            globalState = new GlobalState(10, players);
            this.gameConfig = createStandardConfig(players); //Create new standard GameConfig
            globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile()); //Set the drawPile of the GlobalState
            globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card
            globalState.nextPlayer = players.get(0); //There currently is no active player, nextplayer will start the game in startGame
            MessageSender.sendInitGameToAll(threadedEinzServer, (ArrayList) gameConfig.allRules,
                    (ArrayList) globalState.getPlayersOrdered());
        }
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

    // TODO: offer getState(username) function that returns the globalstate and the playerstate (or maybe two functions for this)
    // TODO: offer onFinishTurn(username) function
    // TODO: offer onCustomAction(user, message) function


    public void initialiseGame(ArrayList<Player> players, HashMap<Card, Integer> deck, Collection<BasicGlobalRule> globalRules,
                               Map<Card, ArrayList<BasicCardRule>> cardRules) {
        if (players.size() < 2 || players.size() > MAX_NUMBER_OF_PLAYERS) {
            //don't initialise game
        } else {
            gameConfig = new GameConfig(deck);
            //gameConfig.allCardsInGame.addAll(deck.keySet()); -> already done in GameConfig
            for (Player p : players) {
                gameConfig.addParticipant(p);
            }
            for (BasicGlobalRule r : globalRules) {
                gameConfig.addGlobalRule(r);
            }
            for (Card c : cardRules.keySet()) {
                for (BasicCardRule r: cardRules.get(c)){
                    gameConfig.assignRuleToCard(r, c);
                }
            }
            globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card
            globalState.nextPlayer = players.get(0); //There currently is no active player, nextplayer will start the game in startGame
            MessageSender.sendInitGameToAll(threadedEinzServer, (ArrayList) gameConfig.allRules,
                    (ArrayList) globalState.getPlayersOrdered());
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
        onChange();
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
        if (!CardRuleChecker.checkIsValidPlayCard(globalState, card, gameConfig)) {
            MessageSender.sendPlayCardResponse(p, threadedEinzServer, false);
            return false;
        } else {
            p.hand.remove(card);
            globalState.addCardToDiscardPile(card);
            CardRuleChecker.checkOnPlayAssignedCard(globalState, card, gameConfig);
            CardRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
            GlobalRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
            MessageSender.sendPlayCardResponse(p, threadedEinzServer, true);

            onChange();
            return true;
        }
    }

    /**
     * OnDrawCard Rules get applied after the player draws cards
     *
     * @param p the player that wants to draw cards
     * @return the Cards that player draws, if he is not allowed to draw cards returns null.
     */
    public ArrayList<Card> drawCards(Player p) {
        if (!globalState.getActivePlayer().equals(p)) {
            MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "It is not your turn.");
            return null; //TODO: Check in rules whether its a players turn
        }
        if (!CardRuleChecker.checkIsValidDrawCards(globalState, gameConfig)) {
            MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "A rule doesn't allow you to draw cards.");
            return null;
        } else {
            ArrayList<Card> result = (ArrayList) globalState.drawCards(globalState.getCardsToDraw());
            p.hand.addAll(result);
            CardRuleChecker.checkOnDrawCard(globalState, gameConfig);
            MessageSender.sendDrawCardResponseSuccess(p, threadedEinzServer, result);

            onChange();
            return result;
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
        globalState.setPlayerFinished(player);
        GlobalRuleChecker.checkOnPlayerFinished(globalState, player, gameConfig);
        //TODO: don't let player finsih but remove him
    }

    /**
     * This function is not listed in the interface and is just used inside this class
     * Still under construction
     * Gets finished once list of possible rules we want to have is (nearly) complete
     *
     * @return a new GameConfig with a standard deck consisting of 112 cards and the standard rules
     */
    private GameConfig createStandardConfig(List<Player> players) {
        Map<Card, Integer> numberOfCardsInGame = new HashMap<>();
        Set<Card> allCardsInGame = new HashSet<>();
        for (CardText ct : CardText.values()) {
            if (ct != CardText.CHANGECOLOR && ct != CardText.CHANGECOLORPLUSFOUR && ct != CardText.DEBUG) {
                for (CardColor cc : CardColor.values()) {
                    if (cc != CardColor.NONE) {
                        Card card = new Card("temp", ct.type, ct, cc); // TODO: #cardtag replace "temp"
                        numberOfCardsInGame.put(card, 2);
                        allCardsInGame.add(card);
                    }
                }
            } else {
                /*
                don't add these cards yet
                TODO: add them later (except the debug card)
                Card card = new Card("temp", ct.type, ct, CardColor.NONE); // #cardtag
                numberOfCardsInGame.put(card, 4);
                allCardsInGame.add(card);
                 */
            }
        }
        GameConfig result = new GameConfig(numberOfCardsInGame);
        //gameConfig.allCardsInGame.addAll(deck.keySet()); -> already done in GameConfig
        for (Player p : players) {
            result.addParticipant(p);
        }

        //Add all necessary GlobalRules: (StartGameWithCardsRule, WinOnNoCardsRule)
        result.addGlobalRule(new StartGameWithCardsRule());
        result.addGlobalRule(new WinOnNoCardsRule());
        result.addGlobalRule(new ResetCardsToDrawRule());
        result.addGlobalRule(new NextTurnRule());

        //Add all necessary CardRules: (ChangeDirectionRule)
        for (CardColor cc : CardColor.values()) {
            if (cc != CardColor.NONE) {
                result.assignRuleToCard(new ChangeDirectionRule(), new Card("temp", CardText.SWITCHORDER.type, CardText.SWITCHORDER, cc));
            }
        }
        return result;
    }

    /**
     * things to do whenever anything in the game changes
     */
    private void onChange() {

        //Check if any player has finished
        for (Player p : globalState.getPlayersOrdered()) {
            if (GlobalRuleChecker.checkIsPlayerFinished(globalState, p, gameConfig)) {
                globalState.setPlayerFinished(p);
                MessageSender.sendPlayerFinishedToAll(p, threadedEinzServer);
            }
        }

        //Send everyone their state
        MessageSender.sendStateToAll(threadedEinzServer, globalState);


    }

}
