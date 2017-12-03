package ch.ethz.inf.vs.a4.minker.einz.keepalive;

public interface OnKeepaliveTimeoutCallback {
    /**
     * Will be called in case of a timeout regarding incoming messages.
     * Make sure to run the reaction in the right thread. Per default this will be called from the keepalive thread(s)
     * * After timeout milliseconds not receiving a message, a timeout will trigger. However, if the sending of a message on our side happens first, there will still be a socket IOException (AND then this timeout)!
     */
    public void onKeepaliveTimeout();
}
