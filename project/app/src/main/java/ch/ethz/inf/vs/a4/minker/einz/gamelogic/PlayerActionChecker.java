package ch.ethz.inf.vs.a4.minker.einz.gamelogic;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.model.GameConfig;
import ch.ethz.inf.vs.a4.minker.einz.model.GlobalState;
import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;


/**
 * Created by Fabian on 07.12.2017.
 */

public class PlayerActionChecker {

    /**
     * checks if a card can be played (without changing anything in the globalState or sending messages)
     *
     * @param card the card to be played
     * @param p    the player that wants to play a card
     * @param state globalState that holds the state of the game
     * @param config gameConfig that holds the configuration of the game
     * @return whether the player is allowed to play the card he wants to play or not
     */
    public static boolean isPlayable(Card card, Player p, GlobalState state, GameConfig config) {
        if (!state.getActivePlayer().equals(p) || !CardRuleChecker.checkIsValidPlayCard(state, card, config)) {
            return false; //TODO: Check in rules whether its a players turn
        } else {
            return true;
        }
    }

    /**
     * checks which cards can be played (without changing anything in the globalState or sending messages)
     * @param p player that wants to play cards
     * @param state globalState that holds the state of the game
     * @param config gameConfig that holds the configuration of the game
     * @return the Cards the player is able to play (If he cant play any cards, returns an empty List)
     */
    public static ArrayList<Card> playableCards (Player p, GlobalState state, GameConfig config){
        ArrayList<Card> result = new ArrayList<>();
        for (Card c: p.hand){
            if (isPlayable(c, p, state, config)){
                result.add(c);
            }
        }
        return result;
    }
}
