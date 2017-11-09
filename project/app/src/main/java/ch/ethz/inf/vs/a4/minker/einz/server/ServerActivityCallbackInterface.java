package ch.ethz.inf.vs.a4.minker.einz.server;

/**
 * Used by the ThreadedEinzServer to update the UI
 */
public interface ServerActivityCallbackInterface {
    public void updateNumClientsUI(int num);
    // public void refreshClientListUI()
}
