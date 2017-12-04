package ch.ethz.inf.vs.a4.minker.einz.keepalive;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKeepaliveMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.Debug;

import java.util.concurrent.*;

/**
 * Handles sending and receiving keepalive packets. make sure to call onAnyMessageReceived and onAnyMessageSent when you do that.
 * Start it by running a threads .start() method with this runnable as content.
 */
public class KeepaliveScheduler implements Runnable {

    private final long timeout;
    private final long timeout_initial_bonus;
    private SendMessageCallback sendMessageCallback;
    private final OnKeepaliveTimeoutCallback onTimeoutCallback;

    private long lastOutTime; // last time a message was sent
    private long lastInTime; // last time a message was received
    private boolean firstInTime = true; // as long as this is true, the timeout will only trigger after the additional initial bonus
    private boolean firstOutTime = true; // dito
    private boolean inTimeoutTriggered = false; // whether there was a timeout regarding incoming messages.
    private ScheduledExecutorService executorIn; // timed threads to check timeout in and out
    private ScheduledExecutorService executorOut;
    private ScheduledFuture<?> futureIn; // used to cancel the pending checks
    private ScheduledFuture<?> futureOut;

    /**
     * Initializes the KeepaliveScheduler. Still need to start it later!
     * After timeout milliseconds not receiving a message, a timeout will trigger. However, if the sending of a message on our side happens first, there will still be a socket IOException (AND then this timeout):
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
                long tempTime = System.currentTimeMillis() - lastOutTime;
                if(tempTime < timeout + bonus){
                    if(Debug.logKeepaliveSpam){
                        Log.d("keepalive", "firstOutTime: "+firstOutTime+"\ntime passed: "+tempTime);
                    }
                    // don't timeout yet, launch new execution
                    launchOutTimeoutChecker(); // yey recursion?!
                } else {
                    if(Debug.logKeepaliveSpam){
                        Log.d("keepalive", "TIMEOUT!\nfirstOutTime: "+firstOutTime+"\ntime passed: "+tempTime);
                    }

                    //send keepalive packet and restart launchOutTimeoutChecker
                    //launchOutTimeoutChecker is run in the same thread (executor) anyways, so it doesn't make a difference if we call it before or after onOutTimeout
                    onOutTimeout();
                    launchOutTimeoutChecker(); // TODO: does this recursion filling the stack need to be considered in terms of memory?
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
        // send keepalive message:
        // if not inTimeout
        // sendMessageCallback.sendMessage("keepalive");

        if(!inTimeoutTriggered){ // if there was a timeout regarding incoming packets, then we don't need to send any more keepalive packets
            EinzMessage<EinzKeepaliveMessageBody> message = new EinzMessage<>(
                    new EinzMessageHeader("networking", "KeepAlive"),
                    new EinzKeepaliveMessageBody()
            );
            try {
                sendMessageCallback.sendMessage(message);
            } catch (SendMessageFailureException e) {
                Log.i("KeepaliveScheduler", "Failed to send keepalive packet. Probably because the client buffer was not yet initialized (or no longer).");
                e.printStackTrace();
                // There should either be a first message sent anyways or the incoming connection should be failing as well,
                // So we don't do anything here
            }

        } // (else die silently)
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
     * stops the internal timeout timers. Blocks until done so.
     */
    private void onShuttingDown(){
        // stop the timers
        if(futureIn!=null){
            futureIn.cancel(false);
        }
        if(futureOut!=null){
            futureOut.cancel(false);
        }
        executorIn.shutdown();
        executorOut.shutdown();
    }

    /**
     * starts itself in a background thread. Returns a reference to that thread.
     */
    public Thread runInParallel(){
        Thread t = new Thread(this);
        t.start();
        return t;
    }

    @Override
    public void run() {
        // initialize executors that are used in launchOutTimeoutChecker and launchInTimeoutChecker
        executorIn = Executors.newSingleThreadScheduledExecutor();
        executorOut = Executors.newSingleThreadScheduledExecutor();

        // start the timers in new threads
        //long a = System.currentTimeMillis();
        //float b = a;
        //Log.d("DEBUGERINO", "long: "+a+"\nfloat: "+b);
        this.lastOutTime = System.currentTimeMillis();
        launchOutTimeoutChecker();
        this.lastInTime = System.currentTimeMillis();
        launchInTimeoutChecker();

    }
}
