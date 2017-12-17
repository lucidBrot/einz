package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.RulesContainer;

/**
 * Created by Josua on 12/8/17.
 */

public class EinzSingleton {
    private static EinzSingleton instance;

    private EinzClient client;
    private final CardLoader cardLoader;
    private final RuleLoader ruleLoader;
    private RulesContainer lastRulesSavedContainer;

    private EinzSingleton(){
        this.cardLoader = new CardLoader();
        this.ruleLoader = new RuleLoader();
    }

    public static EinzSingleton getInstance(){
        if(instance == null){
            instance = new EinzSingleton();
        }
        return instance;
    }

    public void setEinzClient(EinzClient client){
        this.client = client;
    }

    public EinzClient getEinzClient(){
        return client;
    }

    public CardLoader getCardLoader() {
        return cardLoader;
    }

    public RuleLoader getRuleLoader() {
        return ruleLoader;
    }

    /**
     * @return <code>null</code> if the container was never set.<br>
     *     This container is used to store the currently selected rule settings after closing the settings in {@link ch.ethz.inf.vs.a4.minker.einz.UI.LobbyActivity}
     *     and to reload them on reopening of the settings
     */
    public RulesContainer getLastRulesSavedContainer() {
        return lastRulesSavedContainer;
    }

    /**
     * @param lastRulesSavedContainer <code>null</code> if the loading settings method should just use whatever it wants instead. Otherwise, set the container to store the currently selected rule settings.
     */
    public void setLastRulesSavedContainer(RulesContainer lastRulesSavedContainer) {
        this.lastRulesSavedContainer = lastRulesSavedContainer;
    }
}
