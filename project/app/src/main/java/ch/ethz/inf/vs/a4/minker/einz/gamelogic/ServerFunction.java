package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.inf.vs.a4.minker.einz.model.BasicRule;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;
import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.ChangeDirectionRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.DrawTwoCardsRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.GameEndsOnWinRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.NextTurnRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayColorRule;
import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.PlayTextRule;
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
    private final boolean DEBUG_MODE;

    public ServerFunction(){
        this(false);
    }
    /**
     * Sets DEBUG_MODE to debugMode
     * only call this with "true" for debugging
     * DEBUG_MODE == true prevents the ServerFunction from using the threadedEinzServer (e.g trying to send messages)
     */
    public ServerFunction(boolean debugMode) {
        this.MAX_NUMBER_OF_PLAYERS = 20;
        DEBUG_MODE = debugMode;
    }

    /**
     * @param maxNumberOfPlayers the maximum number of Players allowed in a game
     */
    public ServerFunction(int maxNumberOfPlayers) {
        this.MAX_NUMBER_OF_PLAYERS = maxNumberOfPlayers;
        DEBUG_MODE = false;
    }

    /**
     * initialises a new game with standard cards and rules
     *
     * @param threadedEinzServer server that holds the list of players and spectators
     * @param players            the players in the game, the players play in the order in which they are in the
     *                           ArrayList (lowest index plays first)
     */

    public void initialiseStandardGame(ThreadedEinzServer threadedEinzServer, ArrayList<Player> players) {
        if (players.size() < 2 || players.size() > MAX_NUMBER_OF_PLAYERS) {
            //don't initialise game
        } else {
            this.threadedEinzServer = threadedEinzServer;
            globalState = new GlobalState(10, players);
            this.gameConfig = createStandardConfig(players); //Create new standard GameConfig
            globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile()); //Set the drawPile of the GlobalState
            globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card
            globalState.nextPlayer = globalState.getPlayersOrdered().get(0); //There currently is no active player, nextplayer will start the game in startGame
            if (!DEBUG_MODE) {
                MessageSender.sendInitGameToAll(threadedEinzServer, gameConfig, (ArrayList) globalState.getPlayersOrdered());
            }
        }
    }

    /**
     * initialises a new game
     *
     * @param threadedEinzServer server that holds the list of players and spectators
     * @param players     the players in the game, the players play in the order in which they are in the
     *                    ArrayList (lowest index plays first)
     * @param deck        contains the specified cards the specified amount of times
     *                    in the HashMap, the Key determines the Card and the Mapped value determines how many times
     *                    that card is put into the game
     * @param globalRules set of global rules with which the game is played
     * @param cardRules   card rules with the card they should apply to
     */

    // TODO: offer onCustomAction(user, message) function

    public void initialiseGame(ThreadedEinzServer threadedEinzServer, ArrayList<Player> players, HashMap<Card, Integer> deck, Collection<BasicGlobalRule> globalRules,
                               Map<Card, ArrayList<BasicCardRule>> cardRules) {
        if (players.size() < 2 || players.size() > MAX_NUMBER_OF_PLAYERS) {
            //don't initialise game
        } else {
            this.threadedEinzServer = threadedEinzServer;
            globalState = new GlobalState(10, players);
            gameConfig = new GameConfig(deck);
            //gameConfig.allCardsInGame.addAll(deck.keySet()); -> already done in GameConfig
            for (Player p : players) {
                gameConfig.addParticipant(p);
            }
            for (BasicGlobalRule r : globalRules) {
                gameConfig.addGlobalRule(r);
            }
            for (Card c : cardRules.keySet()) {
                for (BasicCardRule r : cardRules.get(c)) {
                    gameConfig.assignRuleToCard(r, c);
                }
            }
            globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile()); //Set the drawPile of the GlobalState
            globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card
            globalState.nextPlayer = globalState.getPlayersOrdered().get(0); //There currently is no active player, nextplayer will start the game in startGame
            if (!DEBUG_MODE) {
                MessageSender.sendInitGameToAll(threadedEinzServer, gameConfig, (ArrayList) globalState.getPlayersOrdered());
            }
        }
    }


    /**
     * Gives the correct amount of cards to each player
     * Sets the active player to the first player to play
     * Lets the players start playing
     */
    public void startGame() {
        // gameConfig is null here if initialiseGame/initialiseStandartGame is not called properly
        GlobalRuleChecker.checkOnStartGame(globalState, gameConfig);
        globalState.nextTurn(); //Sets the active player to the one specified in initialiseGame
        onChange();
    }

    /**
     * player p wants to play a card, his card is only played if the rules allow him to.
     * OnPlayRules get applied after the player plays his card
     *
     * @param card the card to be played
     * @param p    the player that wants to play a card
     * @return whether the player is allowed to play the card he wants to play or not
     */
    public boolean play(Card card, Player p) {
        if (!globalState.getActivePlayer().equals(p)) {
            if (!DEBUG_MODE) {
                MessageSender.sendPlayCardResponse(p, threadedEinzServer, false);
            }
            return false; //TODO: Check in rules whether its a players turn
        }
        if (!CardRuleChecker.checkIsValidPlayCard(globalState, card, gameConfig)) {
            if (!DEBUG_MODE) {
                MessageSender.sendPlayCardResponse(p, threadedEinzServer, false);
            }
            return false;
        } else {
            p.hand.remove(card);
            globalState.addCardToDiscardPile(card);
            CardRuleChecker.checkOnPlayAssignedCard(globalState, card, gameConfig);
            CardRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
            GlobalRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
            if (!DEBUG_MODE) {
                MessageSender.sendPlayCardResponse(p, threadedEinzServer, true);
            }

            onChange();
            return true;
        }
    }

    /**
     * @param p player that wants to end his turn
     * @return whether he is allowed to end his turn (and therefore did)
     */
    public boolean finishTurn(Player p) {
        if (!globalState.getActivePlayer().equals(p) || !GlobalRuleChecker.checkIsValidEndTurn(globalState, p, gameConfig)) {
            //The player isnt allowed to end his turn
            return false;
        } else {
            GlobalRuleChecker.checkOnEndTurn(globalState, gameConfig);
            globalState.nextTurn(); //I have to call this here since we can't make a rule for that
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
            if (!DEBUG_MODE) {
                MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "It is not your turn.");
            }
            return null; //TODO: Check in rules whether its a players turn
        }
        if (!CardRuleChecker.checkIsValidDrawCards(globalState, gameConfig)) {
            if (!DEBUG_MODE) {
                MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "A rule doesn't allow you to draw cards.");
            }
            return null;
        } else {
            ArrayList<Card> result = (ArrayList) globalState.drawCards(globalState.getCardsToDraw());
            p.hand.addAll(result);
            CardRuleChecker.checkOnDrawCard(globalState, gameConfig);
            if (!DEBUG_MODE) {
                MessageSender.sendDrawCardResponseSuccess(p, threadedEinzServer, result);
            }

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
                        Card card = new Card(cc + "_" + ct.indicator, ct.type, ct, cc, "drawable", "card_" + ct.indicator + "_" + cc);
                        numberOfCardsInGame.put(card, 2);
                        allCardsInGame.add(card);
                    }
                }
            } else {
                /*
                don't add these cards yet
                TODO: add these cards as soon as wishing a color works
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

        //Add all necessary GlobalRules
        result.addGlobalRule(new GameEndsOnWinRule());
        result.addGlobalRule(new ResetCardsToDrawRule());

        StartGameWithCardsRule rule = new StartGameWithCardsRule();
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("Number Of Cards", 7);
            rule.setParameter(parameter);
        } catch (JSONException e) {
            throw new RuntimeException();
        }
        result.addGlobalRule(rule);

        result.addGlobalRule(new WinOnNoCardsRule());

        //Add all necessary CardRules
        for (CardText ct : CardText.values()) {
            if (ct != CardText.CHANGECOLOR && ct != CardText.CHANGECOLORPLUSFOUR && ct != CardText.DEBUG) {
                for (CardColor cc : CardColor.values()) {
                    if (cc != CardColor.NONE) {
                        Card card = new Card(cc + "_" + ct.indicator, ct.type, ct, cc, "drawable", "card_" + ct.indicator + "_" + cc);
                        //assign rules to the cards
                        result.assignRuleToCard(new PlayColorRule(), card);
                        result.assignRuleToCard(new PlayTextRule(), card);
                    }
                }
            } else {
                /*
                add play rules for these cards later (once they get added to the game)
                TODO: add rules as soon as wishing a color works
                Card card = new Card("temp", ct.type, ct, CardColor.NONE); // #cardtag

                        result.assignRuleToCard(new PlayAlwaysRule(), card);
                 */
            }
        }

        for (CardColor cc : CardColor.values()) {
            if (cc != CardColor.NONE) {
                result.assignRuleToCard(new ChangeDirectionRule(), new Card(cc + "_" + CardText.SWITCHORDER.indicator, CardText.SWITCHORDER.type,
                        CardText.SWITCHORDER, cc, "drawable", "card_" + CardText.SWITCHORDER.indicator + "_" + cc));
                result.assignRuleToCard(new DrawTwoCardsRule(), new Card(cc + "_" + CardText.PLUSTWO.indicator, CardText.PLUSTWO.type,
                        CardText.PLUSTWO, cc, "drawable", "card_" + CardText.PLUSTWO.indicator + "_" + cc));
            }
        }

        //ADD THIS LAST SO EFFECTS HAPPEN BEFORE THE PLAYERS TURN IS FINISHED
        result.addGlobalRule(new NextTurnRule());

        //Initialise all the rules with the globalState
        for (BasicRule r : result.allRules) {
            r.initialize(result);
        }
        return result;
    }

    /**
     * things to do whenever anything in the game changes
     */
    private void onChange() {

        //Check if any player has finished
        for (Player p : globalState.getPlayersOrdered()) {
            if (GlobalRuleChecker.checkIsPlayerFinished(globalState, p)) {
                globalState.setPlayerFinished(p);
                if (!DEBUG_MODE) {
                    MessageSender.sendPlayerFinishedToAll(p, threadedEinzServer);
                }
            }
        }

        //Send everyone their state
        if (!DEBUG_MODE) {
            MessageSender.sendStateToAll(threadedEinzServer, globalState, gameConfig);
        }

    }

    /**
     * sends the state of the game to a player
     * only sends him what he can know
     *
     * @param p the player that wants to receive the state of the game
     */
    public void getState(Player p) {
        if (!DEBUG_MODE) {
            MessageSender.sendState(p, threadedEinzServer, globalState, gameConfig);
        }
    }

    public GlobalState getGlobalState() {
        if (!DEBUG_MODE) {
            return null;
        } else
            return globalState;
    }

}
