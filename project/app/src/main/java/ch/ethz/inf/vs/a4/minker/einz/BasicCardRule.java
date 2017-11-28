package ch.ethz.inf.vs.a4.minker.einz;

/**
 * Basic Rule Class for rules applicable to cards.
 */
public abstract class BasicCardRule extends BasicRule{

    /**
     * The Card which the rule belongs to
     */
    protected Card assignedTo;

    /**
     * Initializes the CardRule and assigns a Card to the rule.
     * <br />
     * Call this rather to initialize(GameConfig config) to initialize the card.
     * @param config The Game config the Rule can refer to
     * @param assignedTo The Card assigned to the Card.
     */
    public void initialize(GameConfig config, Card assignedTo) {
        super.initialize(config);
        this.assignedTo = assignedTo;
    }


    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * In permissive mode at least one rule has to return true to allow the player to draw a card.
     * @param state
     * @return True if the player is allowed to
     */
    public boolean isValidDrawCardsPermissive(GlobalState state){
        return false;
    }

    /**
     * Checks whether the player is allowed to draw cards.
     * <br />
     * In restrictive mode every rule has to return true to allow the player to draw a card.
     * @param state
     * @return False if the player is not allowed to.
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
