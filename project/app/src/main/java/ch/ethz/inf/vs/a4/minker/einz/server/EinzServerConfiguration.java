package ch.ethz.inf.vs.a4.minker.einz.server;

import java.util.ArrayList;

/**
 * Stores Configuration of {@link ThreadedEinzServer} that is not suited for the communications-only part, because it has to do with the content of the messages,
 * But is also not really relevant to the serverlogic
 */
public class EinzServerConfiguration {
    protected String adminUsername;
    protected ArrayList<String> getConnectedUsers(){
        return null; // TODO: move connected array from parent to here, as well as other settings
    }
}
