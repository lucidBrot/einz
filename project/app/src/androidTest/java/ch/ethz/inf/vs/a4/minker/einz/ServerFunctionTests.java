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
    public void constructorTest(){
        GameState gameState = new GameState();


        assertEquals(gameState.getNumberOfPlayers(), 2);
        assertEquals(gameState.getThreatenedCards(), 1);
        assertEquals("1000", gameState.getActivePlayer().IP);

    }


}
