package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerFunction;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Fabian on 10.11.2017.
 */

public class ServerFunctionTests {

    @Test
    public void constructorTest() {
        GameState gameState = new GameState();

        assertEquals(gameState.getPlayers().size(), 2);
        assertEquals(gameState.getThreatenedCards(), 1);
        assertEquals("1000", gameState.getActivePlayer().IP);

    }

    @Test
    public void drawTest() {
        GameState gameState = new GameState();
        ServerActivityCallbackInterface dummySACI = new ServerActivityCallbackInterface() {
            @Override
            public void updateNumClientsUI(int num) {

            }
        };
        ServerFunction serverFunction = new ServerFunction();
        ThreadedEinzServer dummyTES = new ThreadedEinzServer(1000, dummySACI, serverFunction);
        serverFunction.server = dummyTES;
        dummyTES.gameState = gameState;

        serverFunction.drawXCards(27, gameState.getPlayers().get(0));
        serverFunction.drawXCards(54, gameState.getPlayers().get(1));

        assertEquals(27 + 7, gameState.getPlayers().get(0).Hand.size());
        assertEquals(54 + 7, gameState.getPlayers().get(1).Hand.size());
    }

    @Test
    public void playTest() {
        GameState gameState = new GameState();
        ServerActivityCallbackInterface dummySACI = new ServerActivityCallbackInterface() {
            @Override
            public void updateNumClientsUI(int num) {

            }
        };
        ServerFunction serverFunction = new ServerFunction();
        ThreadedEinzServer dummyTES = new ThreadedEinzServer(1000, dummySACI, serverFunction);
        serverFunction.server = dummyTES;
        dummyTES.gameState = gameState;


        int count[] = new int[2];
        count[0] = 7;
        count[1] = 7;
        int APN;

        for (int i = 0; i < 50; i++) {
            boolean hasPlayed = false;
            Player p = gameState.getActivePlayer();
            String logString = p.name+": ";
            for (Card card : p.Hand){
                logString = logString + "("+card.color+","+card.type+","+card.wish+"), ";
            }
            logString.substring(0, logString.length()-2);
            Card c = serverFunction.topCard();
            Log.i("State1", "topCard: ("+c.color+","+c.type+","+c.wish+")");
            Log.i("State2", logString);
            if (p.name.equals("Peter")) {
                APN = 0;
            } else {
                APN = 1;
            }
            for (Card card : p.Hand) {
                if (serverFunction.isPlayable(card, p) && hasPlayed == false) {
                    if (card.type.equals(CardTypes.CHANGECOLORPLUSFOUR) ||
                            card.type.equals(CardTypes.CHANGECOLOR)){
                        card.wish =  CardColors.BLUE;
                    }
                    serverFunction.play(card, p);
                    count[APN] = count[APN] - 1;
                    hasPlayed = true;
                    break;
                }
            }
            if (!hasPlayed){
                if (!serverFunction.hasDrawn()) {
                    if (serverFunction.cardsToDraw() == 1) {
                        serverFunction.drawOneCard(p);
                        count[APN] = count[APN] + 1;
                    } else {
                        count[APN] = count[APN] + serverFunction.cardsToDraw();
                        serverFunction.drawXCards(serverFunction.cardsToDraw(), p);
                    }
                } else {
                    gameState.nextPlayer();
                }
            } else {
                if (serverFunction.hasWon(p)){
                    serverFunction.endGame();
                    i = 50;
                }
            }
        }

        assertEquals(count[0], gameState.getPlayers().get(0).Hand.size());
        assertEquals(count[1], gameState.getPlayers().get(1).Hand.size());


    }

    @Test
    public void playTestWith4Players(){
        ArrayList<Player> mplayers = new ArrayList<>(4);
        mplayers.add(new Player("Donald","1000"));
        mplayers.add(new Player("Tick","2000"));
        mplayers.add(new Player("Trick","3000"));
        mplayers.add(new Player("Track","4000"));
        GameState gameState = new GameState(mplayers);
        ServerActivityCallbackInterface dummySACI = new ServerActivityCallbackInterface() {
            @Override
            public void updateNumClientsUI(int num) {

            }
        };
        ServerFunction serverFunction = new ServerFunction();
        ThreadedEinzServer dummyTES = new ThreadedEinzServer(1000, dummySACI, serverFunction);
        serverFunction.server = dummyTES;
        dummyTES.gameState = gameState;

        int count[] = new int[4];
        count[0] = 7;
        count[1] = 7;
        count[2] = 7;
        count[3] = 7;
        int APN;
        for (int i = 0; i < 50; i++) {
            boolean hasPlayed = false;
            Player p = gameState.getActivePlayer();
            String logString = p.name+": ";
            for (Card card : p.Hand){
                logString = logString + "("+card.color+","+card.type+","+card.wish+"), ";
            }
            logString.substring(0, logString.length()-2);
            Card c = serverFunction.topCard();
            Log.i("State1", "topCard: ("+c.color+","+c.type+","+c.wish+")");
            Log.i("State2", logString);
            if (p.name.equals("Donald")) {
                APN = 0;
            } else if (p.name.equals("Tick")){
                APN = 1;
            } else if (p.name.equals("Trick")) {
                APN = 2;
            } else {
                APN =3;
            }
            for (Card card : p.Hand) {
                if (serverFunction.isPlayable(card, p) && hasPlayed == false) {
                    if (card.type.equals(CardTypes.CHANGECOLORPLUSFOUR) ||
                            card.type.equals(CardTypes.CHANGECOLOR)){
                        card.wish =  CardColors.BLUE;
                    }
                    serverFunction.play(card, p);
                    count[APN] = count[APN] - 1;
                    hasPlayed = true;
                    break;
                }
            }
            if (!hasPlayed){
                if (!serverFunction.hasDrawn()) {
                    if (serverFunction.cardsToDraw() == 1) {
                        serverFunction.drawOneCard(p);
                        count[APN] = count[APN] + 1;
                    } else {
                        count[APN] = count[APN] + serverFunction.cardsToDraw();
                        serverFunction.drawXCards(serverFunction.cardsToDraw(), p);
                    }
                } else {
                    gameState.nextPlayer();
                }
            } else {
                if (serverFunction.hasWon(p)){
                    serverFunction.endGame();
                    i = 50;
                }
            }
        }

        assertEquals(count[0], gameState.getPlayers().get(0).Hand.size());
        assertEquals(count[1], gameState.getPlayers().get(1).Hand.size());
        assertEquals(count[2], gameState.getPlayers().get(2).Hand.size());
        assertEquals(count[3], gameState.getPlayers().get(3).Hand.size());


    }
}