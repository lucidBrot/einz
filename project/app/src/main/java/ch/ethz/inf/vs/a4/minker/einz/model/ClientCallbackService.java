package ch.ethz.inf.vs.a4.minker.einz.model;

import java.util.List;

/**
 * Created by Josua on 12/4/17.
 */

public interface ClientCallbackService {
    /**
     * Calls the client of the given Player and lets it choose from a given list.
     * @param player The player who has to choose
     * @param options The options the player can choose from
     *
     * @return The chosen String
     */
    String getSelectionFromPlayer(String ruleName, Player player, List<String> options);
}
