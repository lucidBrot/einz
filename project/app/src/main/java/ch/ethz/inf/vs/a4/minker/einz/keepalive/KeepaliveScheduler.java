package ch.ethz.inf.vs.a4.minker.einz.keepalive;

import java.util.concurrent.*;

public class KeepaliveScheduler implements Runnable {

    private final long timeout;
    private final long timeout_initial_bonus;
    private SendMessageCallback sendMessageCallback;
    private final OnKeepaliveTimeoutCallback onTimeoutCallback;

    private float lastOutTime; // last time a message was sent
    private float lastInTime; // last time a message was received
    private boolean firstInTime = true; // as long as this is true, the timeout will only trigger after the additional initial bonus
    private boolean firstOutTime = true; // dito
    private boolean inTimeoutTriggered = false; // whether there was a timeout regarding incoming messages.
    private ScheduledExecutorService executorIn; // timed threads to check timeout in and out
    private ScheduledExecutorService executorOut;
    private ScheduledFuture<?> futureIn; // used to cancel the pending checks
    private ScheduledFuture<?> futureOut;

    /**
     * @param timeout The time in ms until the socket is closed unless there were incoming messages during this timespan.
     *                This is also the time in ms until this class sends a new keepalive message if there were no outgoing messages during this timespan.
     *                See protocols/server_thoughts.md on github for more details.
     * @param timeout_initial_bonus The first timer will start counting down from <literal>timeout + timeout_initial_bonus</literal> in case you want to give the initialization phase more time without instantly timing out.
     * @param sendMessageCallback A interface to send messages to the other party.
     * @param onTimeoutCallback A interface to be called when the keepalive timeout is triggered. You are responsible that this is run in the main thread if you need it to.
     */
    public KeepaliveScheduler(long timeout, long timeout_initial_bonus, SendMessageCallback sendMessageCallback, OnKeepaliveTimeoutCallback onTimeoutCallback){

        this.timeout = timeout;
        this.timeout_initial_bonus = timeout_initial_bonus;
        this.sendMessageCallback = sendMessageCallback;
        this.onTimeoutCallback = onTimeoutCallback;
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
     * the checker should after every timeout countdown check if there was actually no new message sent since then. If yes, it will send a keepalive packet
     * There is always only one checker for out timeouts.
     *
     */
    private void launchOutTimeoutChecker(){
        Runnable check = new Runnable() {
            @Override
            public void run() {
                float bonus = 0;
                if(firstOutTime){bonus = timeout_initial_bonus;}
                if(System.currentTimeMillis() - lastOutTime < timeout + bonus){
                    // don't timeout yet, launch new execution
                    launchOutTimeoutChecker(); // yey recursion?!
                } else {
                    onOutTimeout();
                }
            }
        };

        // before the first message, give a bonus of timeout_initial_bonus
        long bonus = 0;
        if(firstOutTime){bonus = timeout_initial_bonus;}
        long time_out = bonus + timeout;
        futureOut = executorOut.schedule(check, time_out, TimeUnit.MILLISECONDS);

        // the task is now scheduled. after the timeout will it check whether it should actually trigger a timeout.
        // the ScheduledFuture could be used to cancel this again
    }

    /**
     * send new keepalive message
     */
    private void onOutTimeout() {
        // TODO: send keepalive message
        // if not inTimeout
        // sendMessageCallback.sendMessage("keepalive");
    }

    /**
     * the checker should after every timeout countdown check if there was actually no new message received since then. If yes, it will cause the timeout onTimeoutCallback to be called
     * There is always only one checker for in timeouts.
     */
    private void launchInTimeoutChecker(){
        Runnable check = new Runnable() {
            @Override
            public void run() {
                float bonus = 0;
                if(firstInTime){bonus = timeout_initial_bonus;}
                if(System.currentTimeMillis() - lastInTime < timeout + bonus){
                    // don't timeout yet, launch new execution
                    launchInTimeoutChecker(); // yey recursion?!
                } else {
                    onInTimeout();
                }
            }
        };

        // before the first message, give a bonus of timeout_initial_bonus
        long bonus = 0;
        if(firstInTime){bonus = timeout_initial_bonus;}
        long time_in = bonus + timeout;
        futureIn = executorIn.schedule(check, time_in, TimeUnit.MILLISECONDS);

        // the task is now scheduled. after the timeout will it check whether it should actually trigger a timeout.
        // the ScheduledFuture could be used to cancel this again
    }

    private void onInTimeout() {
        this.inTimeoutTriggered = true;
        this.onTimeoutCallback.onKeepaliveTimeout();
    }

    /**
     * stops the internal timeout timers
     */
    private void onShuttingDown(){
        // stop the timers
        if(futureIn!=null){
            futureIn.cancel(false);
        }
        if(futureOut!=null){
            futureOut.cancel(false);
        }
    }

    @Override
    public void run() {
        // initialize executors that are used in launchOutTimeoutChecker and launchInTimeoutChecker
        executorIn = Executors.newSingleThreadScheduledExecutor();
        executorOut = Executors.newSingleThreadScheduledExecutor();

        // start the timers in new threads
        this.lastOutTime = System.currentTimeMillis();
        launchOutTimeoutChecker();
        this.lastInTime = System.currentTimeMillis();
        launchInTimeoutChecker();

    }
}
