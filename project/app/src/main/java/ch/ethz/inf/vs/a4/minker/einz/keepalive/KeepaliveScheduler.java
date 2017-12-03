package ch.ethz.inf.vs.a4.minker.einz.keepalive;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;

import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

public class KeepaliveScheduler implements Runnable {

    private final int timeout;
    private final int timeout_initial_bonus;
    private SendMessageCallback sendMessageCallback;
    private final OnKeepaliveTimeoutCallback callback;

    private float lastOutTime; // last time a message was sent
    private float lastInTime; // last time a message was received
    private boolean firstInTime = true; // as long as this is true, the timeout will only trigger after the additional initial bonus
    private boolean firstOutTime = true; // dito

    /**
     * @param timeout The time in ms until the socket is closed unless there were incoming messages during this timespan.
     *                This is also the time in ms until this class sends a new keepalive message if there were no outgoing messages during this timespan.
     *                See protocols/server_thoughts.md on github for more details.
     * @param timeout_initial_bonus The first timer will start counting down from <literal>timeout + timeout_initial_bonus</literal> in case you want to give the initialization phase more time without instantly timing out.
     * @param sendMessageCallback A interface to send messages to the other party.
     * @param callback A interface to be called when the keepalive timeout is triggered. You are responsible that this is run in the main thread if you need it to.
     */
    public KeepaliveScheduler(int timeout, int timeout_initial_bonus, SendMessageCallback sendMessageCallback, OnKeepaliveTimeoutCallback callback){

        this.timeout = timeout;
        this.timeout_initial_bonus = timeout_initial_bonus;
        this.sendMessageCallback = sendMessageCallback;
        this.callback = callback;
    }

    /**
     * Does the needed handling but not more (i.e. does no parsing or such).
     * Call this function when you receive any message.
     * This function resets the keepaliveInTimer back to the specified timeout maximum.
     * (<br>Even if the message is a keepalive message, you will have to parse it)
     */
    public void onAnyMessageReceived(){
        this.firstInTime = false;
        this.lastInTime = System.currentTimeMillis();
    }

    /**
     * Sets the internal timeout timer for outgoing messages back to maximum and starts to countdown again.
     */
    public void onAnyMessageSent(){
        this.firstOutTime = false;
        this.lastOutTime = System.currentTimeMillis();
    }

    /**
     * // the checker should after every timeout countdown check if there was actually no new message sent since then. If yes, it will send a keepalive packet
     */
    private void launchOutTimeoutChecker(){
        final Executor executor = Executors.newSingleThreadScheduledExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                float bonus = 0;
                if(firstOutTime){bonus = timeout_initial_bonus;}
                if(System.currentTimeMillis() - lastInTime < timeout + bonus){
                    // don't timeout yet, launch new execution
                    executor.execute(this); // yey recursion?
                } else {
                    onOutTimeout();
                }
            }
        });
    }

    /**
     * send new keepalive message
     */
    private void onOutTimeout() {
        // TODO: send keepalive message
        // sendMessageCallback.sendMessage("keepalive");
    }

    @Override
    public void run() {

    }
}
