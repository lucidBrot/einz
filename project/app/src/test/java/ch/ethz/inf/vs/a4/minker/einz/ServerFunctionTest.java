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
    public void initialiseStandardGameTest (){
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Peter"));
        players.add(new Player("Paul"));
        ServerFunction s = new ServerFunction(true);
        s.initialiseStandardGame(null, players);
        s.startGame();
    }


    @Test
    public void playTest(){
        ArrayList<Player> players = new ArrayList<>();
        Player peter = new Player("Peter");
        Player paul = new Player("Paul");
        players.add(peter);
        players.add(paul);
        ServerFunction s = new ServerFunction(true);
        s.initialiseStandardGame(null, players);
        s.startGame();

        for(Card c: peter.hand) {
            s.play(c, peter);

            Log.i("peters hand: ", peter.hand.toString());
            Log.i("pauls hand: ", paul.hand.toString());
            Log.i("topCardDiscardPile: ", s.getGlobalState().getTopCardDiscardPile().getName());

        }

    }

}
