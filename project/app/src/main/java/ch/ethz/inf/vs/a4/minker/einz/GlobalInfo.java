package ch.ethz.inf.vs.a4.minker.einz;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Fabian on 17.11.2017.
 */

public class GlobalInfo {
    HashMap<Player,Integer> cardAmount;
    ArrayList<Card> topOfPlayPile;
    //The players theoretically only need to know the
    // top card of the playPile but it's convenient being able to look through the top ~10 cards
    Player activePlayer;
    int cardsToDraw;
}
