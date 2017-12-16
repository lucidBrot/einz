package ch.ethz.inf.vs.a4.minker.einz.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Josua on 12/4/17.
 */

public interface SelectorRule {
    /**
     * Returns a list of choices from which the user has to choose one from. The choice will be sent
     * to the server. The rule maps a value to a screen Text
     * This is a method used by the client
     * @param state Current game-state.
     * @return A map of choices (values to be sent, Screen Text)
     */
    Map<String, String> getChoices(GlobalState state);

    String getSelectionTitle();

    JSONObject makeSelectionReadyForSend(String selection) throws JSONException;

    GlobalState onPlayAssignedCardChoice(GlobalState state, JSONObject rulePlayParams);
}