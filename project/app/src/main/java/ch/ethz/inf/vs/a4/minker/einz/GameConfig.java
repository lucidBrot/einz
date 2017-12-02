package ch.ethz.inf.vs.a4.minker.einz;

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

    private Map<Card,Set<BasicCardRule>> rulePerCard;

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
        Set<BasicCardRule> ruleSet = rulePerCard.get(card);
        if(ruleSet == null){
            ruleSet = new HashSet<>();
            rulePerCard.put(card, ruleSet);
        }
        ruleSet.add(rule);
        allRules.add(rule);
    }

    public void addGlobalRule(BasicGlobalRule rule){
        globalRules.add(rule);
        allRules.add(rule);
    }

    public Set<BasicCardRule> getRulesForCard(Card card){
        return rulePerCard.get(card);
    }

    public List<Card> getShuffledDrawPile(){
        List<Card> shuffledDrawPile = new LinkedList<>();
        shuffledDrawPile.addAll(drawPile);
        Collections.shuffle(shuffledDrawPile);
        return shuffledDrawPile;
    }


}
