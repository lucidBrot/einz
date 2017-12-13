package ch.ethz.inf.vs.a4.minker.einz.model;

import java.util.List;

/**
 * Created by Josua on 12/4/17.
 */

public interface SelectorRule {
    /**
     * Returns a list of choices from which the user has to choose one from. The choice will be sent
     * to the server.
     * This is a method used by the client
     * @param state Current game-state.
     * @return A List of choices
     */
    List<String> getChoices(GlobalState state);
}