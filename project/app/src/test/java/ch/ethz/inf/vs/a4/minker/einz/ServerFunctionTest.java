package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.model.Player;

import org.junit.Test;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;

/**
 * Created by Fabian on 04.12.2017.
 */

public class ServerFunctionTest {
    @Test
    public void initialiseStandardGameTest() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Peter"));
        players.add(new Player("Paul"));
        ServerFunction s = new ServerFunction(true);
        s.initialiseStandardGame(null, players);
        s.startGame();
    }


    @Test
    public void playTest() {
        ArrayList<Player> players = new ArrayList<>();
        Player peter = new Player("Peter");
        Player paul = new Player("Paul");
        players.add(peter);
        players.add(paul);
        ServerFunction s = new ServerFunction(true);
        s.initialiseStandardGame(null, players);
        s.startGame();

        int barrier = 100;
        while (!s.getGlobalState().isGameFinished() && barrier > 0) {
            for (Player p : s.getGlobalState().getPlayersOrdered()) {
                int tries = p.hand.size();
                barrier--;
                try {
                    for (int i = 0; i < tries; i++) {
                        s.play(p.hand.get(i), p);
                        if (i == tries - 1) {
                            s.drawCards(p);
                            s.finishTurn(p);
                        }
                    }
                } catch (Exception e){

                }
            }
        }
        Log.i("Endstate","");

        //TODO: Add a rule for when a player can just end his turn?
        //TODO: playing a plus2 card sets the cardsToDraw to 8
    }

}
