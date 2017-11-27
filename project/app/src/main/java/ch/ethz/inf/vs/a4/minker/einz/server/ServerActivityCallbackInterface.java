package ch.ethz.inf.vs.a4.minker.einz.server;

/**
 * Used by the ThreadedEinzServer to update the UI
 */
public interface ServerActivityCallbackInterface {
    public void updateNumClientsUI(int num);
    // public void refreshClientListUI()
    /**
     * When you are the host and the server is ready to accept connections
     */
    public void onLocalServerReady();

    /**
     * When you are the host and the first client-handler in the server is ready to receive the register message
     */
    public void onFirstESCHReady();
}
