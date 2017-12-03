package ch.ethz.inf.vs.a4.minker.einz.keepalive;

public interface OnKeepaliveTimeoutCallback {
    /**
     * Will be called in case of a timeout regarding incoming messages.
     * Make sure to run the reaction in the right thread. Per default this will be called from the keepalive thread(s)
     */
    public void onKeepaliveTimeout();
}
