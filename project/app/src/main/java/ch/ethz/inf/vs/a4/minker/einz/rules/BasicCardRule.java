package ch.ethz.inf.vs.a4.minker.einz.rules;

import ch.ethz.inf.vs.a4.minker.einz.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.GlobalState;

/**
 * Basic Rule Class for rules applicable to cards.
 */
public abstract class BasicCardRule extends BasicRule{

    /**
     * The Card which the rule belongs to
     */
    protected Card assignedTo;

    /**
     * @return the card this rules belongs to
     */
    public Card getAssignedTo(){
        return assignedTo;
    }


    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * In permissive mode at least one rule has to return true to allow the player to draw a card.
     * @param state
     * @return True if he is allowed to
     */
    public boolean isValidDrawCardsPermissive(GlobalState state){
        return false;
    }

    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * In restrictive mode every rule has to return true to allow the player to draw a card.
     * @param state
     * @return False if he is not allowed to.
     */
    public boolean isValidDrawCardsRestrictive(GlobalState state){
        return true;
    }

    /**
     * Checks whether the card that the player wants to play is valid.
     * <br />
     * In permissive mode at least one rule has to return true to allow the player to draw a card.
     * @param state
     * @param played
     * @return False
     */
    public boolean isValidPlayCardPermissive(GlobalState state, Card played){
        return false;
    }

    /**
     * Checks whether the card that the player wants to play is valid.
     * <br />
     * In restrictive mode every rule has to return true to allow the player to draw a card.
     * @param state
     * @param played
     * @return
     */
    public boolean isValidPlayCardRestrictive(GlobalState state, Card played){
        return true;
    }





    /**
     * Called after a card player decides to draw a card
     * @param state
     * @return
     */
    public GlobalState onDrawCard(GlobalState state){
        return state;
    }

    /**
     * Called if a player played the card assigned with the rule
     * @param state
     * @param played
     * @return
     */
    public GlobalState onPlayAssignedCard(GlobalState state, Card played){
        return state;
    }

    /**
     * Called on any card the player plays. This includes the card assigned to this rule.
     * @param state
     * @param played
     * @return
     */
    public GlobalState onPlayAnyCard(GlobalState state, Card played){
        return state;
    }

}
