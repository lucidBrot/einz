package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.model.Player;
import org.junit.Test;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;

/**
 * Created by Fabian on 04.12.2017.
 */

public class ServerFunctionTest {
    @Test
    public void initialiseStandardGameTest (){
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Peter"));
        players.add(new Player("Paul"));
        ServerFunction s = new ServerFunction();
        s.initialiseStandardGame(players);
        s.startGame();
    }

    @Test
    public void playTest(){
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Peter"));
        players.add(new Player("Paul"));
        ServerFunction s = new ServerFunction();
        s.initialiseStandardGame(players);
        s.startGame();

    }
}
