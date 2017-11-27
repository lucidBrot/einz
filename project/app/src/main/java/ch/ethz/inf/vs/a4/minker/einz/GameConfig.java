package ch.ethz.inf.vs.a4.minker.einz;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Josua on 11/23/17.
 */

public class GameConfig {

    private List<Card> drawPile;


    public Map<Card,Set<BasicRule>> rulePerCard;
    public Set<BasicRule> allRules;
    public Set<Card> allCardsInGame;
    public List<Participant> allParticipants;


    public GameConfig(Map<Card, Integer> numberOfCardsInGame){
        this.allCardsInGame = new HashSet<>();

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


    public List<Card> getShuffledDrawPile(){
        List<Card> shuffledDrawPile = new LinkedList<Card>();
        shuffledDrawPile.addAll(drawPile);
        Collections.shuffle(shuffledDrawPile);
        return shuffledDrawPile;
    }


}
