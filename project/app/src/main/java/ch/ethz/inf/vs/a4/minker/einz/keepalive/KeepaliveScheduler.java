package ch.ethz.inf.vs.a4.minker.einz.keepalive;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Globals;
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

    //<editor-fold desc="Comment on how the calculations work">
    // After a message was received, the receiver waits INCOMING_TIMEOUT until the next message.
    // So we have to send the next message INCOMING_TIMEOUT after the previous message, assuming the network is of stable speed.
    // But because it is not, we need to state how much additional ping we want to allow, using the MAX_PING_FLUCTUATION
    // when the network is suddenly faster and then slower, each by MAX_PING_FLUCTUATION, then the connection should still be considered (just) alive.
    // So at worst fluctuation, the receiver will receive one packet at time t and one packet at time t+SENDING_INTERVAL+(2*MAX_PING_FLUCTUATION)
    // This means we need to have
    // SENDING_INTERVAL+2*MAX_PING_FLUCTUATION <= INCOMING_TIMEOUT
    //
    // And to support a maximal ping of MAX_PING_SUPPORTED we need to have
    // MAX_PING_SUPPORTED <= INITIAL_INCOMING_TIMEOUT
    // Because once the whole system is working, we always have a message underway, and no matter the toatal ping,
    // there should always be a message incoming within the timespan supported (unless we have heavy fluctuation)
    //
    // To support MAX_PING_SUPPORTED, we need to ensure that the first message has the time to reach the target.
    // We do this by initially giving more time until we get the first message:
    // MAX_SUPPORTED_PING <= INCOMING_TIMEOUT + INITIAL_BONUS
    //
    // So we choose MAX_SUPPORTED_PING and INCOMING_TIMEOUT, e.g. 1000 ms and 3000 ms
    // ==> INITIAL_BONUS = MAX_SUPPORTED_PING - INCOMING_TIMEOUT, i.e. for our example negative, thus no bonus is needed
    //
    // And we choose MAX_PING_FLUCTUATION, e.g. 100 ms
    // ==> SENDING_INTERVAL = INCOMING_TIMEOUT - 2*MAX_PING_FLUCTUATION, i.e for our example positive and thus possible: 2800 ms
    //
    // In a completely stable network, it is ok to have SENDING_INTERVAL equal INCOMING_TIMEOUT, but because messageparsing takes also some time,
    // it would be better to also allow some GRACE_PERIOD difference.

    /**
     * Internally, the {@link ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler} does the following:
     * Every time a message is sent or received, it stores the current time
     * in {@link ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler#lastInTime}
     * and {@link ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler#lastOutTime}.
     * <p>
     * A parallel thread checks every CHECK_TIMEOUT ms whether a timeout should be triggered.
     * For this, using the Nyquist-Shannon theorem, CHECK_TIMEOUT must be at most half of INCOMING_TIMEOUT and/or SENDING_INTERVAL
     * That way, we will always notice within the INCOMING_TIMEOUT/SENDING_INTERVAL that we need to react.
     **/

    // When choosing the INCOMING_TIMEOUT, it is a tradeoff between how fast we notice somebody has lost connection and how fast
    // we need to send messages.
    // I think a good compromise would be to notice loss of connection after one second:
    // ==> INCOMING_TIMEOUT = 1000 (chosen)
    // ==> MAX_SUPPORTED_PING = 1000 (chosen)
    // ==> MAX_PING_FLUCTUATION = 100
    // ==> INITIAL_BONUS = 0
    // ==> SENDING_INTERVAL = 800
    // ==> CHECK_OUT_INTERVAL = SENDING_INTERVAL/2
    // ==> CHECK_IN_INTERVAL = INCOMING_TIMEOUT/2
    //</editor-fold>


    private final long CHECK_OUT_INTERVAL;
    private final long CHECK_IN_INTERVAL;
    private final long SENDING_INTERVAL;
    private final long INCOMING_TIMEOUT;
    private final int MAX_SUPPORTED_PING;
    private final int MAX_PING_FLUCTUATION;
    private final long INITIAL_BONUS; // only on the receiving part
    private SendMessageCallback sendMessageCallback;
    private final OnKeepaliveTimeoutCallback onTimeoutCallback;

    private long lastOutTime; // last time a message was sent
    private long lastInTime; // last time a message was received
    private boolean firstInTime = true; // as long as this is true, the timeout will only trigger after the additional initial bonus

    private boolean inTimeoutTriggered = false; // whether there was a timeout regarding incoming messages.
    private ScheduledExecutorService executorIn; // timed threads to check timeout in and out
    private ScheduledExecutorService executorOut;
    private ScheduledFuture<?> futureIn; // used to cancel the pending checks
    private ScheduledFuture<?> futureOut;


    // When choosing the INCOMING_TIMEOUT, it is a tradeoff between how fast we notice somebody has lost connection and how fast
    // we need to send messages.
    // I think a good compromise would be to notice loss of connection after one second. Example:
    // ==> INCOMING_TIMEOUT = 1000 (chosen)
    // ==> MAX_SUPPORTED_PING = 1000 (chosen)
    // ==> MAX_PING_FLUCTUATION = 100
    // ==> INITIAL_BONUS = 0
    // ==> SENDING_INTERVAL = 800
    // ==> CHECK_OUT_INTERVAL = SENDING_INTERVAL/.2
    // ==> CHECK_IN_INTERVAL = INCOMING_TIMEOUT/.2

    /**
     * Initializes the KeepaliveScheduler. Still need to start it later!
     * After timeout milliseconds not receiving a message, a timeout will trigger. However, if the sending of a message on our side happens first, there will still be a socket IOException (AND then this timeout):
     * Calculates internal things based on your specifications here.
     * This constructor will crash at runtime if you pass it retarded arguments!
     *
     * @param incomingTimeout     The time in ms until the socket is closed unless there were incoming messages during this timespan.
     *                            This is also the time in ms until this class sends a new keepalive message if there were no outgoing messages during this timespan.
     *                            See protocols/server_thoughts.md on github for more details.
     * @param maxSupportedPing    The maximal ping that should still be able to reliably connect. Larger pings <i>might</i> work if they are still within the fluctuation region
     * @param maxPingFluctuation  (from the expected ping both up- and/or downwards)
     * @param sendMessageCallback A interface to send messages to the other party.
     * @param onTimeoutCallback   A interface to be called when the keepalive timeout is triggered. You are responsible that this is run in the main thread if you need it to.
     */
    public KeepaliveScheduler(long incomingTimeout, int maxSupportedPing, int maxPingFluctuation, SendMessageCallback sendMessageCallback, OnKeepaliveTimeoutCallback onTimeoutCallback) {
        INCOMING_TIMEOUT = incomingTimeout;
        MAX_SUPPORTED_PING = maxSupportedPing;
        MAX_PING_FLUCTUATION = maxPingFluctuation;
        this.sendMessageCallback = sendMessageCallback;
        this.onTimeoutCallback = onTimeoutCallback;
        if (INCOMING_TIMEOUT < 0 || MAX_PING_FLUCTUATION < 0 || MAX_SUPPORTED_PING < 0 || sendMessageCallback == null || onTimeoutCallback == null) {
            throw new RuntimeException("Bad arguments for KeepaliveScheduler");
        }

        long initialBonus = MAX_SUPPORTED_PING - INCOMING_TIMEOUT;
        if (initialBonus < 0) {
            // no bonus needed
            INITIAL_BONUS = 0;
        } else {
            INITIAL_BONUS = initialBonus;
        }

        SENDING_INTERVAL = INCOMING_TIMEOUT/2L - 2L * MAX_PING_FLUCTUATION - Globals.KEEPALIVE_GRACE_PERIOD;
        if (SENDING_INTERVAL <= 0) {
            // wtf are you doing
            throw new RuntimeException("Choose INCOMING_TIMEOUT for keepalive larger than 2*MAX_PING_FLUCTUATION!");
        }

        CHECK_OUT_INTERVAL = SENDING_INTERVAL / 2L;
        CHECK_IN_INTERVAL = INCOMING_TIMEOUT / 2L;
    }

    /**
     * chooses default settings (see Globals.java):
     * Example:
     * INCOMING_TIMEOUT = 1000 (chosen)
     * MAX_SUPPORTED_PING = 1000 (chosen)
     * MAX_PING_FLUCTUATION = 100
     * INITIAL_BONUS = 0
     * SENDING_INTERVAL = 800
     * CHECK_OUT_INTERVAL = SENDING_INTERVAL/2
     * CHECK_IN_INTERVAL = INCOMING_TIMEOUT/2
     *
     * @param sendMessageCallback A interface to send messages to the other party.
     * @param onTimeoutCallback   A interface to be called when the keepalive timeout is triggered. You are responsible that this is run in the main thread if you need it to.
     */
    public KeepaliveScheduler(SendMessageCallback sendMessageCallback, OnKeepaliveTimeoutCallback onTimeoutCallback) {
        this(Globals.KEEPALIVE_DEFAULT_INCOMING_TIMEOUT, Globals.KEEPALIVE_DEFAULT_MAX_SUPPORTED_PING, Globals.KEEPALIVE_DEFAULT_MAX_PING_FLUCTUATION,
                sendMessageCallback, onTimeoutCallback);
    }

    /**
     * Does the needed handling but not more (i.e. does no parsing or such).
     * Call this function when you receive any message.
     * This function resets the keepaliveInTimer back to the specified timeout maximum.
     * (<br>Even if the message is a keepalive message, you will have to parse it)
     */
    public void onAnyMessageReceived() {
        if (Debug.logKeepaliveSpam) {
            Log.d("keepalive", "received anymessage.");
        }
        this.firstInTime = false;
        this.lastInTime = System.currentTimeMillis();
    }

    /**
     * Sets the internal timeout timer for outgoing messages back to maximum and starts to countdown again.
     */
    public void onAnyMessageSent() {
        if (Debug.logKeepaliveSpam) {
            Log.d("keepalive", "anymessage sent.");
        }
        this.lastOutTime = System.currentTimeMillis();
    }

    /**
     * the checker should after every timeout countdown check if there was actually no new message sent since then. If yes, it will send a keepalive packet
     * There is always only one checker for out timeouts.
     */
    private void launchOutTimeoutChecker() {
        Runnable check = new Runnable() {
            @Override
            public void run() {
                long tempTime = System.currentTimeMillis() - lastOutTime;
                if (tempTime < SENDING_INTERVAL) {
                    // don't timeout yet, launch new execution
                    if(Debug.logKeepaliveSpam){
                        Log.d("keepalive", "Not yet time to timeoutOut.\ntempTime: "+tempTime);
                    }
                    launchOutTimeoutChecker(); // yey recursion?!
                } else {
                    //send keepalive packet and restart launchOutTimeoutChecker
                    //launchOutTimeoutChecker is run in the same thread (executor) anyways, so it doesn't make a difference if we call it before or after onOutTimeout
                    if(Debug.logKeepaliveSpam){
                        Log.d("keepalive", "TimeOUTOUT!\ntempTime: "+tempTime);
                    }
                    onOutTimeout();
                    // does this recursion filling the stack need to be considered in terms of memory?
                }
            }
        };

        futureOut = executorOut.schedule(check, CHECK_OUT_INTERVAL, TimeUnit.MILLISECONDS);

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

        if (!inTimeoutTriggered) { // if there was a timeout regarding incoming packets, then we don't need to send any more keepalive packets
            EinzMessage<EinzKeepaliveMessageBody> message = new EinzMessage<>(
                    new EinzMessageHeader("networking", "KeepAlive"),
                    new EinzKeepaliveMessageBody()
            );
            try {
                sendMessageCallback.sendMessage(message);
                if (Debug.logKeepalivePackets) {
                    Log.d("keepalive", "sent keepalive.");
                }
            } catch (SendMessageFailureException e) {
                Log.i("KeepaliveScheduler", "Failed to send keepalive packet. Probably because the client buffer was not yet initialized (or no longer).");
                // e.printStackTrace();
                // There should either be a first message sent anyways or the incoming connection should be failing as well,
                // So we don't do anything here
            }

            launchOutTimeoutChecker();
        } else { // (else die silently)
            if (Debug.logKeepaliveSpam) {
                Log.d("keeaplive", "timeout in was triggered so I'm not reacting to timeout out");
            }
        }
    }

    /**
     * the checker should after every timeout countdown check if there was actually no new message received since then. If yes, it will cause the timeout onTimeoutCallback to be called
     * There is always only one checker for in timeouts.
     */
    private void launchInTimeoutChecker() {
        Runnable check = new Runnable() {
            @Override
            public void run() {
                long bonus = 0;
                if (firstInTime) {
                    bonus = INITIAL_BONUS;
                }
                long tempTime = System.currentTimeMillis() - lastInTime;
                if (tempTime < INCOMING_TIMEOUT + bonus) {
                    if (Debug.logKeepaliveSpam) {
                        Log.d("keepalive", "firstInTime: " + firstInTime + "\ntime passed: " + tempTime+"\nlastIn: "+lastInTime);
                    }
                    // don't timeout yet, launch new execution
                    launchInTimeoutChecker(); // yey recursion?!
                } else {
                    if (Debug.logKeepaliveSpam) {
                        Log.d("keepalive", "TIMEOUT! firstInTime: " + firstInTime + "\ntime passed: " + tempTime+"\nlastIn: "+lastInTime);
                    }
                    onInTimeout();
                }
            }
        };

        // before the first message, give a bonus of INITIAL_BONUS
        long bonus = 0;
        if (firstInTime) {
            bonus = INITIAL_BONUS;
        }
        long time_in = bonus + CHECK_IN_INTERVAL;
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
    private void onShuttingDown() {
        // stop the timers
        if (futureIn != null) {
            futureIn.cancel(false);
        }
        if (futureOut != null) {
            futureOut.cancel(false);
        }
        if (executorIn != null) {
            executorIn.shutdown();
        }
        if (executorOut != null) {
            executorOut.shutdown();
        }
    }

    /**
     * starts itself in a background thread. Returns a reference to that thread.
     */
    public Thread runInParallel() {
        Thread t = new Thread(this);
        t.start();
        return t;
    }

    @Override
    public void run() {

        if (!Debug.useKeepalive) {
            return; // do not use keepalive
        }

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
