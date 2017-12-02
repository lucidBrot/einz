package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;

import org.junit.Test;

import java.util.ArrayList;

import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Fabian on 10.11.2017.
 */

public class ServerFunctionTests {
    //Fix this after new_datamodell has been established
/*
    @Test
    public void constructorTest() {
        GameState gameState = new GameState();

        assertEquals(gameState.getPlayersOrdered().size(), 2);
        assertEquals(gameState.getThreatenedCards(), 1);

    }

    @Test
    public void drawTest() {
        GameState gameState = new GameState();
        ServerFunction serverFunction = new ServerFunction(gameState);

        serverFunction.drawXCards(27, gameState.getPlayersOrdered().get(0));
        serverFunction.drawXCards(54, gameState.getPlayersOrdered().get(1));

        assertEquals(27 + 7, gameState.getPlayersOrdered().get(0).hand.size());
        assertEquals(54 + 7, gameState.getPlayersOrdered().get(1).hand.size());
    }


    //INVALID TEST: Players now get removed from the game once they have won
    @Test
    public void playTest() {
        GameState gameState = new GameState();
        ServerFunction serverFunction = new ServerFunction(gameState);


        int count[] = new int[2];
        count[0] = 7;
        count[1] = 7;
        int APN;

        for (int i = 0; i < 50; i++) {
            boolean hasPlayed = false;
            Player p = gameState.getActivePlayer();
            String logString = p.getName()+": ";
            for (Card card : p.hand){
                logString = logString + "("+card.color+","+card.text+","+card.wish+"), ";
            }
            logString.substring(0, logString.length()-2);
            Card c = serverFunction.topCard();
            Log.i("State1", "topCard: ("+c.color+","+c.text+","+c.wish+")");
            Log.i("State2", logString);
            if (p.getName().equals("Peter")) {
                APN = 0;
            } else {
                APN = 1;
            }
            for (Card card : p.hand) {
                if (serverFunction.isPlayable(card, p) && hasPlayed == false) {
                    if (card.text.equals(CardText.CHANGECOLORPLUSFOUR) ||
                            card.text.equals(CardText.CHANGECOLOR)){
                        card.wish =  CardColor.BLUE;
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

        assertEquals(count[0], gameState.getPlayersOrdered().get(0).hand.size());
        assertEquals(count[1], gameState.getPlayersOrdered().get(1).hand.size());


    }

    //INVALID TEST: Players now get removed from the game once they have won
    @Test
    public void playTestWith4Players(){
        ArrayList<Player> mplayers = new ArrayList<>(4);
        mplayers.add(new Player("Donald"));
        mplayers.add(new Player("Tick"));
        mplayers.add(new Player("Trick"));
        mplayers.add(new Player("Track"));
        GameState gameState = new GameState(mplayers, null);
        ServerFunction serverFunction = new ServerFunction(gameState);
        int count[] = new int[4];
        count[0] = 7;
        count[1] = 7;
        count[2] = 7;
        count[3] = 7;
        int APN;
        for (int i = 0; i < 200; i++) {
            boolean hasPlayed = false;
            Player p = gameState.getActivePlayer();
            String logString = p.getName()+": ";
            for (Card card : p.hand){
                logString = logString + "("+card.color+","+card.text+","+card.wish+"), ";
            }
            logString.substring(0, logString.length()-2);
            Card c = serverFunction.topCard();
            Log.i("State1", "topCard: ("+c.color+","+c.text+","+c.wish+")");
            Log.i("State2", logString);
            if (p.getName().equals("Donald")) {
                APN = 0;
            } else if (p.getName().equals("Tick")){
                APN = 1;
            } else if (p.getName().equals("Trick")) {
                APN = 2;
            } else {
                APN =3;
            }
            for (Card card : p.hand) {
                if (serverFunction.isPlayable(card, p) && hasPlayed == false) {
                    if (card.text.equals(CardText.CHANGECOLORPLUSFOUR) ||
                            card.text.equals(CardText.CHANGECOLOR)){
                        card.wish =  CardColor.BLUE;
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
                    i = 200;
                }
            }
        }

        assertEquals(count[0], gameState.getPlayersOrdered().get(0).hand.size());
        assertEquals(count[1], gameState.getPlayersOrdered().get(1).hand.size());
        assertEquals(count[2], gameState.getPlayersOrdered().get(2).hand.size());
        assertEquals(count[3], gameState.getPlayersOrdered().get(3).hand.size());


    }
*/
}

