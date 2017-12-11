package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzCustomActionAction;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzCustomActionMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicCardRule;
import ch.ethz.inf.vs.a4.minker.einz.model.BasicGlobalRule;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;

/**
 * Created by Fabian on 09.11.2017.
 */

public interface ServerFunctionDefinition {

    /**
     * initialises a new game with standard cards and rules
     *
     * @param threadedEinzServer server that holds the list of players and spectators
     * @param players            the players in the game, the players play in the order in which they are in the
     *                           ArrayList (lowest index plays first)
     */
    public void initialiseStandardGame(ThreadedEinzServer threadedEinzServer, ArrayList<Player> players);

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
    public void initialiseGame(ThreadedEinzServer threadedEinzServer, ArrayList<Player> players, HashMap<Card, Integer> deck, Collection<BasicGlobalRule> globalRules, Map<Card, ArrayList<BasicCardRule>> cardRules);


    /**
     * Gives the correct amount of cards to each player
     * Sets the active player to the first player to play
     * Lets the players start playing
     */
    public void startGame();

    /**
     * player p wants to play a card, his card is only played if the rules allow him to.
     *
     * @param card the card to be played
     * @param p    the player that wants to playe a card
     * @return whether the player is allowed to play the card he wants to play or not
     */
    public boolean play(Card card, Player p);

    /**
     * @param p the player that wants to draw cards
     * @return the Cards that player draws, otherwise returns null.
     */
    public ArrayList<Card> drawCards(Player p);

    /**
     * ends the running game
     * not sure what this does exactly
     */
    public void endGame();

    /**
     * removes a Player from the game
     * If there are less than two players left after removing the Player, the game is ended automatically.
     *
     * @param p the player to be removed
     */
    public void removePlayer(Player p);

    /**
     * Not yet doing much. See implementation
     * @param user
     * @param message
     */
    public void onCustomActionMessage(String user, EinzMessage<EinzCustomActionMessageBody> message);

    public boolean finishTurn(Player p);

}