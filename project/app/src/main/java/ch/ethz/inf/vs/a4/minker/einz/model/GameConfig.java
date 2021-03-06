package ch.ethz.inf.vs.a4.minker.einz.model;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Josua on 11/23/17.
 */

public class GameConfig {

    public List<BasicRule> allRules;
    public List<BasicGlobalRule> globalRules;
    public Set<Card> allCardsInGame;
    public List<Participant> allParticipants;


    private List<Card> drawPile;

    private Map<String,Set<BasicCardRule>> rulePerCard;

    public GameConfig(Map<Card, Integer> numberOfCardsInGame){
        this.allCardsInGame = new HashSet<>();
        this.rulePerCard = new HashMap<>();
        this.allParticipants = new LinkedList<>();
        this.allRules = new ArrayList<>();
        this.globalRules = new ArrayList<>();

        this.drawPile = new LinkedList<>();

        if (numberOfCardsInGame.size() == 0){
            throw new IllegalArgumentException("At least one card has to be in the draw pile");
        }

        for (Card card : numberOfCardsInGame.keySet()){
            this.allCardsInGame.add(card);
            for(int i = 0; i < numberOfCardsInGame.get(card); i++){
                this.drawPile.add(card);
            }
        }
    }

    public void addParticipant(Participant participant){
        allParticipants.add(participant);
    }

    public void assignRuleToCard(BasicCardRule rule, Card card){
        Set<BasicCardRule> ruleSet = rulePerCard.get(card.getID());
        if(ruleSet == null){
            ruleSet = new HashSet<>();
            rulePerCard.put(card.getID(), ruleSet);
        }
        if(Debug.logRuleSpam) {
            Log.d("GameConfig", "rule: " + (rule == null ? "null" : rule.toString()));
        }
        rule.assignedTo = card;
        ruleSet.add(rule);
        allRules.add(rule);
    }

    public void addGlobalRule(BasicGlobalRule rule){
        globalRules.add(rule);
        allRules.add(rule);
    }

    public Set<BasicCardRule> getRulesForCard(Card card){
        if (rulePerCard.get(card.getID()) == null){
            return new HashSet<>();
        }
        return new HashSet<>(rulePerCard.get(card.getID()));
    }

    public List<Card> getShuffledDrawPile(){
        List<Card> shuffledDrawPile = new LinkedList<>();
        shuffledDrawPile.addAll(drawPile);
        Collections.shuffle(shuffledDrawPile);
        return shuffledDrawPile;
    }

}
