package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.model.Player;

import org.json.JSONException;
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
        //initializeCardLoader();
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player("Peter"));
        players.add(new Player("Paul"));
        ServerFunction s = new ServerFunction(true);
        s.initialiseStandardGame(null, players);
        s.startGame();
    }

    /*
    private void initializeCardLoader() {
        CardLoader loader = EinzSingleton.getInstance().getCardLoader();
        try {
            loader.loadCardsFromResourceFile(this, R.raw.card_definition);
        } catch (JSONException e) {
            Log.e("MainActivity", "Failed to initialize CardLoader.");
            e.printStackTrace();
        }
    }
    */


    @Test
    public void playTestStandardGame() {
        //initializeCardLoader();
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
                           // s.finishTurn(p); WITH CURRENT DEFAULTRULES, THIS ISNT NEEDED
                        }
                    }
                } catch (IndexOutOfBoundsException e){
                    Log.i("Exception","");

                }
            }
        }
        Log.i("Endstate","");

        //Add a rule for when a player can just end his turn? -> with current ruleset not necessary
        //playing a plus2 card sets the cardsToDraw to 8 -> hopefully fixed
        //add a "skip" rule -> added
        //add a "isValidDrawCards" rule -> not very sophisticated but should work
        //TODO: card_definition.json
    }

}
