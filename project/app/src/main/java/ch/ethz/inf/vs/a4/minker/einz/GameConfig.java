package ch.ethz.inf.vs.a4.minker.einz;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Josua on 11/23/17.
 */

public class GameConfig {

    Map<Card,Set<BasicRule>> rulePerCard;

    Set<BasicRule> allRules;

    Set<Card> allCardsInGame;

    List<Participant> allParticipants;

}
