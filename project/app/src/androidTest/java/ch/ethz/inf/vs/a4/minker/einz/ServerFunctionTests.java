package ch.ethz.inf.vs.a4.minker.einz;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import ch.ethz.inf.vs.a4.minker.einz.server.GameState;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivity;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Fabian on 10.11.2017.
 */

public class ServerFunctionTests {
    @Test
    public void addRemovePlayersTest(){

        GameState gameState = new GameState();
        gameState.addPlayer("1000", "Peter");
        gameState.addPlayer("1200", "Paul1");
        gameState.addPlayer("1200", "Paul2");

        assertEquals(gameState.getNumberOfPlayers(), 2);

        gameState.removePlayer("1100");
        gameState.removePlayer("1200");
        gameState.removePlayer("1000");
        gameState.removePlayer("1100");

        assertEquals(gameState.getNumberOfPlayers(), 0);

    }

    @Test
    public void dealCards(){
        GameState gameState = new GameState();
        //add players
        gameState.addPlayer("1000", "Peter");
        gameState.addPlayer("1200", "Paul");
        //initialise drawPile



    }
}
