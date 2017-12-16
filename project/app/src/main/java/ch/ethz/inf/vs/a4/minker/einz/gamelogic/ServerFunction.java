package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionMessageBody;

import ch.ethz.inf.vs.a4.minker.einz.rules.defaultrules.*;
import ch.ethz.inf.vs.a4.minker.einz.rules.otherrules.CountNumberOfCardsAsPoints;

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
import ch.ethz.inf.vs.a4.minker.einz.rules.otherrules.SwapHandCardRule;
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
     * Sets DEBUG_MODE to debugMode
     * only call this with "true" for debugging
     * DEBUG_MODE == true prevents the ServerFunction from using the threadedEinzServer (e.g trying to send messages)
     */
    public ServerFunction() {
        this.MAX_NUMBER_OF_PLAYERS = 20;
    }

    /**
     * @param maxNumberOfPlayers the maximum number of Players allowed in a game
     */
    public ServerFunction(int maxNumberOfPlayers) {
        this.MAX_NUMBER_OF_PLAYERS = maxNumberOfPlayers;
    }

    /**
     * initialises a new game with standard cards and rules
     *
     * @param threadedEinzServer server that holds the list of players and spectators
     * @param players            the players in the game, the players play in the order in which they are in the
     *                           ArrayList (lowest index plays first)
     */

    public void initialiseStandardGame(ThreadedEinzServer threadedEinzServer, ArrayList<Player> players) {
        if (players.size() < 1 || players.size() > MAX_NUMBER_OF_PLAYERS) {
            //don't initialise game
            //if you do nothing here, it crashes
            //don't call initialiseStandardGame with to small/large players.size()
        } else {
            this.threadedEinzServer = threadedEinzServer;
            globalState = new GlobalState(10, players);
            this.gameConfig = createStandardConfig(players); //Create new standard GameConfig
            globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile()); //Set the drawPile of the GlobalState
            while(globalState.getDiscardPile() == null || globalState.getDiscardPile().size() < 1 ||
                    globalState.getTopCardDiscardPile().getColor() == CardColor.NONE) {
                globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card without an origin
            }
            globalState.nextPlayer = globalState.getPlayersOrdered().get(0); //There currently is no active player, nextplayer will start the game in startGame
            MessageSender.sendInitGameToAll(threadedEinzServer, gameConfig, new ArrayList<>(globalState.getPlayersOrdered()));
        }
    }

    /**
     * initialises a new game
     *
     * @param threadedEinzServer server that holds the list of players and spectators
     * @param players            the players in the game, the players play in the order in which they are in the
     *                           ArrayList (lowest index plays first)
     * @param deck               contains the specified cards the specified amount of times
     *                           in the HashMap, the Key determines the Card and the Mapped value determines how many times
     *                           that card is put into the game
     * @param globalRules        set of global rules with which the game is played
     * @param cardRules          card rules with the card they should apply to
     */

    public void initialiseGame(ThreadedEinzServer threadedEinzServer, ArrayList<Player> players, HashMap<Card, Integer> deck, Collection<BasicGlobalRule> globalRules,
                               Map<Card, ArrayList<BasicCardRule>> cardRules) {
        if (players.size() < 1 || players.size() > MAX_NUMBER_OF_PLAYERS) {
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
            //Initialise all the rules with the gameConfig
            for (BasicRule r : gameConfig.allRules) {
                r.initialize(gameConfig);
            }
            globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile()); //Set the drawPile of the GlobalState
            while(globalState.getDiscardPile() == null || globalState.getDiscardPile().size() < 1 ||
                    globalState.getTopCardDiscardPile().getColor() == CardColor.NONE) {
                globalState.addCardsToDiscardPile(globalState.drawCards(1)); //Set the starting card without an origin
            }
            globalState.nextPlayer = globalState.getPlayersOrdered().get(0); //There currently is no active player, nextplayer will start the game in startGame

            MessageSender.sendInitGameToAll(threadedEinzServer, gameConfig, new ArrayList<>(globalState.getPlayersOrdered()));
        }
    }


    /**
     * Gives the correct amount of cards to each player
     * Sets the active player to the first player to play
     * Lets the players start playing
     */
    public void startGame() {
        // gameConfig is null here if initialiseGame/initialiseStandartGame is not called properly
        globalState = GlobalRuleChecker.checkOnStartGame(globalState, gameConfig);
        globalState.nextTurn(); //Sets the active player to the one specified in initialiseGame
        onChange(); //this is not really needed since it gets called in drawCard already when
        // players get their starting hand, but since it doesnt hurt to call onChange to much I still leave this here
    }

    /**
     * player p wants to play a card, his card is only played if the rules allow him to.
     * OnPlayRules get applied after the player plays his card
     *
     * @param card   the card to be played
     * @param player the player that wants to play a card
     * @return whether the player is allowed to play the card he wants to play or not
     */
    public boolean play(Card card, Player player, JSONObject playParameters) {
        if (globalState.isGameFinished()) {
            MessageSender.sendPlayCardResponse(player, threadedEinzServer, false);
            return false;
        }
        Player wantsToPlay = new Player("~Player");
        for (Player p : globalState.getPlayersOrdered()) {
            if (player.getName().equals(p.getName())) {
                wantsToPlay = p;
            }
        }
        //Check if the player even has the card he wants to play
        boolean hasCard = false;
        for (Card c : wantsToPlay.hand) {
            if (c.getID().equals(card.getID())) {
                hasCard = true;
            }
        }

        Player activePlayer = globalState.getActivePlayer();
        if (activePlayer == null || !activePlayer.getName().equals(wantsToPlay.getName()) || !hasCard) {
            MessageSender.sendPlayCardResponse(player, threadedEinzServer, false);
            return false; //TODO: Check in rules whether its a players turn
        }
        if (!CardRuleChecker.checkIsValidPlayCard(globalState, card, gameConfig)) {
            MessageSender.sendPlayCardResponse(player, threadedEinzServer, false);
            return false;
        } else {
            try {
                activePlayer.removeCardFromHandWhereIDMatches(card);
                card.setOrigin(player.getName());
                globalState.addCardToDiscardPile(card);
                globalState = CardRuleChecker.checkOnPlayAssignedCardChoice(globalState, card, gameConfig, playParameters);
                globalState = CardRuleChecker.checkOnPlayAssignedCard(globalState, card, gameConfig);
                globalState = CardRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
                globalState = GlobalRuleChecker.checkOnPlayAnyCard(globalState, card, gameConfig);
                globalState.setLastRuleSelection(playParameters);
                MessageSender.sendPlayCardResponse(player, threadedEinzServer, true);
                onChange();
                return true;
            } catch (Exception e) {
                MessageSender.sendPlayCardResponse(player, threadedEinzServer, false);
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * calls {@link #play(Card, Player, JSONObject)} without a JSONObject for playParameters
     */
    public boolean play(Card card, Player p) {
        return play(card, p, new JSONObject());
    }

    /**
     * @param p player that wants to end his turn
     * @return whether he is allowed to end his turn (and therefore did)
     */
    public boolean finishTurn(Player p) {
        if (globalState.getActivePlayer() == null || !globalState.getActivePlayer().getName().equals(p.getName()) || !GlobalRuleChecker.checkIsValidEndTurn(globalState, p, gameConfig)) {
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
        if (globalState.isGameFinished()) {
            MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "The game has finished.");
            return null;
        }
        Player player = globalState.getActivePlayer();
        if (player == null || !player.getName().equals(p.getName())) {
            MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "It is not your turn.");
            return null; //TODO: Check in rules whether its a players turn
        }
        if (!CardRuleChecker.checkIsValidDrawCards(globalState, gameConfig)) {
            MessageSender.sendDrawCardResponseFailure(p, threadedEinzServer, "A rule doesn't allow you to draw cards.");
            return null;
        } else {
            List<Card> resultList = globalState.drawCards(globalState.getCardsToDraw());
            if (resultList == null) {
                globalState.addCardsToDrawPile(gameConfig.getShuffledDrawPile());
                resultList = globalState.drawCards(globalState.getCardsToDraw());
            }
            ArrayList<Card> result = new ArrayList<>();
            for (Card c : resultList) {
                result.add(c);
            } //Build Arraylist form list since casting causes an exception
            player.hand.addAll(result);
            globalState = CardRuleChecker.checkOnDrawCard(globalState, gameConfig);
            globalState = GlobalRuleChecker.checkOnDrawCard(globalState, gameConfig);
            MessageSender.sendDrawCardResponseSuccess(player, threadedEinzServer, result);

            onChange();
            return result;
        }
    }

    /**
     * ends the running game
     * send everyone the EndGame message
     */
    public void endGame() {
        globalState.finishGame();
        globalState = CardRuleChecker.checkOnGameOver(globalState, gameConfig);
        globalState = GlobalRuleChecker.checkOnGameOver(globalState, gameConfig);
        MessageSender.sendEndGameToAll(globalState, threadedEinzServer);
        threadedEinzServer.onGameOver();
    }

    /**
     * removes a Player from the game
     * If there are less than two players left after removing the Player, the game is ended automatically.
     *
     * @param player the player to be removed
     */
    public void removePlayer(Player player) {
        globalState.removePlayer(player);
        if (globalState.getPlayersOrdered().size() < 1) {
            endGame();
        }
    }

    /**
     * This function is not listed in the interface and is just used inside this class
     *
     * @return a new GameConfig with a standard deck consisting of 112 cards and the standard rules
     */
    private GameConfig createStandardConfig(List<Player> players) { //TODO: This is an ugly function. clean up

        /* Create and add all cards to the game
         *
         * -2 copies of numbers 0-9, rev, skip, take2 for each color
         * -4 copies of take4, choose
         */
        Map<Card, Integer> numberOfCardsInGame = new HashMap<>();
        Set<Card> allCardsInGame = new HashSet<>();
        CardLoader cardLoader = EinzSingleton.getInstance().getCardLoader();
        for (CardText ct : CardText.values()) {
            switch (ct) {
                case DEBUG:
                    break; // don't add this card
                case CHANGECOLOR:
                    Card changeColorCard = cardLoader.getCardInstance("choose");
                    numberOfCardsInGame.put(changeColorCard, 4);
                    allCardsInGame.add(changeColorCard);
                    break;
                case CHANGECOLORPLUSFOUR:
                    Card changeColorPlusPlusFourCard = cardLoader.getCardInstance("take4");
                    numberOfCardsInGame.put(changeColorPlusPlusFourCard, 4);
                    allCardsInGame.add(changeColorPlusPlusFourCard);
                    break;
                default: //usually we want 2 copies of a card of each color
                    for (CardColor cc : CardColor.values()) {
                        if (cc != CardColor.NONE /*&& cc == CardColor.BLUE*/) { // <Debug> can set here to only use blue colors
                            Card card = cardLoader.getCardInstance(cc.toString().toLowerCase() + "_" + ct.indicator);
                            numberOfCardsInGame.put(card, 2);
                            allCardsInGame.add(card);
                        }

                    }
                    break;

            }

        }
        //Create the gameConfig with the cards and  add the players to it
        //numberOfCardsInGame.put(cardLoader.getCardInstance("take4"), 200); // DEBUG

        GameConfig result = new GameConfig(numberOfCardsInGame);
        for (Player p : players) {
            result.addParticipant(p);
        }


        //Add all necessary CardRules
        for (CardText ct : CardText.values()) {
            if (ct != CardText.CHANGECOLOR && ct != CardText.CHANGECOLORPLUSFOUR && ct != CardText.DEBUG) {
                for (CardColor cc : CardColor.values()) {
                    if (cc != CardColor.NONE) {
                        Card card = cardLoader.getCardInstance(cc.toString().toLowerCase() + "_" + ct.indicator);
                        //assign rules to the cards
                        result.assignRuleToCard(new PlayColorRule(), card);
                        result.assignRuleToCard(new PlayTextRule(), card);
                        if(ct.equals(CardText.ZERO)){
                            result.assignRuleToCard(new SwapHandCardRule(), card);
                        }
                    }
                }
            } else if (ct != CardText.DEBUG) {
                String id;
                switch (ct) {
                    case CHANGECOLOR:
                        id = "choose";
                        break;
                    case CHANGECOLORPLUSFOUR:
                        id = "take4";
                        break;
                    default:
                        id = "debug";//unexpected
                        break;
                }
                // id = CardColor.NONE.toString().toLowerCase() + "_" + ct.indicator // seems wrong >.>
                Card card = cardLoader.getCardInstance(id);
                result.assignRuleToCard(new PlayAlwaysRule(), card);
                result.assignRuleToCard(new WishColorRule(), card);
            }
        }

        for (CardColor cc : CardColor.values()) {
            //Create a new rule for each color
            DrawCardsRule drawCardsRule = new DrawCardsRule();
            JSONObject drawCardsRuleParameter = new JSONObject();
            try {
                drawCardsRuleParameter.put("Cards to draw", 2);
                drawCardsRule.setParameter(drawCardsRuleParameter);
            } catch (JSONException e) {
                throw new RuntimeException();
            }

            if (cc != CardColor.NONE) {
                result.assignRuleToCard(new ChangeDirectionRule(), cardLoader.getCardInstance(cc.toString().toLowerCase() + "_" + CardText.SWITCHORDER.indicator));
                result.assignRuleToCard(drawCardsRule, cardLoader.getCardInstance(cc.toString().toLowerCase() + "_" + CardText.PLUSTWO.indicator));
                result.assignRuleToCard(new SkipRule(), cardLoader.getCardInstance(cc.toString().toLowerCase() + "_" + CardText.STOP.indicator));
                //It might make sense to somewhere specify all IDs that exist, so that we don't have to guess
            } else {
                DrawCardsRule drawCardsRule2 = new DrawCardsRule();
                JSONObject drawCardsRule2Parameter = new JSONObject();
                try {
                    drawCardsRule2Parameter = new JSONObject();
                    drawCardsRule2Parameter.put("Cards to draw", 4);
                    drawCardsRule2.setParameter(drawCardsRule2Parameter);
                } catch (JSONException e) {
                    throw new RuntimeException();
                }
                result.assignRuleToCard(drawCardsRule2, cardLoader.getCardInstance(CardText.CHANGECOLORPLUSFOUR.indicator));

            }
        }
        //result.assignRuleToCard(new PlayAlwaysRule(), cardLoader.getCardInstance("choose"));
        //result.assignRuleToCard(new WishColorRule(), cardLoader.getCardInstance("choose"));
        result.assignRuleToCard(new IsValidDrawRule(),
                cardLoader.getCardInstance(CardColor.YELLOW.toString().toLowerCase() + "_" + CardText.ZERO.indicator));


        /*Add all necessary GlobalRules
         *
         * -GameEndsOnWinRule: As soon as one player is finished, the game ends
         * -ResetCardsToDrawRule: After a player draws cards, the cardsToDraw get reset to one
         * -CountNumberOfCardsAsPoints: Displays the number of cards of each player after the game finished
         * -StartGameWithCardsRule: Each player starts the game with 7 cards
         * -WinOnNoCardsRule: If a player has no cards in hand, he wins (is finished)
         * -NextTurnRule: After a player plays a card, his turn is finished
         * -NextTurnRule2: After a player draws a card, his turn is finished
         */
        result.addGlobalRule(new GameEndsOnWinRule());
        result.addGlobalRule(new ResetCardsToDrawRule());
        result.addGlobalRule(new CountNumberOfCardsAsPoints()); // TODO: maybe rather use CountRankFinishedAsPoints or a different (new) rule to count value of cards.
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

        //ADD THESE LAST SO EFFECTS HAPPEN BEFORE THE PLAYERS TURN IS FINISHED
        result.addGlobalRule(new NextTurnRule());
        result.addGlobalRule(new NextTurnRule2());

        //Initialise all the rules with the gameConfig and return sthe result
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

            if (GlobalRuleChecker.checkIsPlayerFinished(globalState, p, gameConfig)) { //this checks whether a player is finished through a rule!
                //If a rule says that a player is finished, set him to finished in the globalState
                //This works now because the GlobalRuleChecker now works as intended
                globalState.setPlayerFinished(p);
                MessageSender.sendPlayerFinishedToAll(p, threadedEinzServer);
                globalState = GlobalRuleChecker.checkOnPlayerFinished(globalState, p, gameConfig);
            }

            //Send everyone their state
            MessageSender.sendStateToAll(threadedEinzServer, globalState, gameConfig);

            //Check if the game is over
            if (globalState.isGameFinished()) {
                endGame();
            }
        }
    }

    /**
     * sends the state of the game to a player
     * only sends him what he can know
     *
     * @param p the player that wants to receive the state of the game
     */

    public void getState(Player p) {
        MessageSender.sendState(p, threadedEinzServer, globalState, gameConfig);
    }

    /**
     * handles any customAction message incoming by passing it on to the rule with that identifier
     *
     * @param user                who issued this
     * @param customActionMessage
     */
    public void onCustomActionMessage(String user, EinzMessage<EinzCustomActionMessageBody> customActionMessage) {
        EinzCustomActionMessageBody body = customActionMessage.getBody();
        String ruleName = body.getRuleName();

        // TODO: call rule based on that message ruleName on CustomActionMessage
        // and reply with CustomActionResponse
    }

}
