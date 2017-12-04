package ch.ethz.inf.vs.a4.minker.einz;

public class Globals {
    public static final String ENCODING = "UTF-8"; // the encoding for sending/receiving strings
    public static final long CLIENT_WAIT_TIME_AFTER_CONNECTION_ESTABLISHED = 500; // twice as long as I think the server will usually take to start after connection established
    public static final int SERVER_SLEEP_TIME_BETWEEN_STOP_LISTENING_AND_CLOSE_SOCKET_ON_SHUTDOWN = 100; // the time running requests are given before the socket closes
    // used in the clients that are not hosting
    // UTF-8 is the probable default of the JVM
    // UTF-32 could work too though
    // https://stackoverflow.com/questions/496321/utf-8-utf-16-and-utf-32
//    UTF-8 has an advantage in the case where ASCII characters represent the majority of characters in a block of text, because UTF-8 encodes all characters into 8 bits (like ASCII). It is also advantageous in that a UTF-8 file containing only ASCII characters has the same encoding as an ASCII file.
//
//            UTF-16 is better where ASCII is not predominant, since it uses 2 bytes per character, primarily. UTF-8 will start to use 3 or more bytes for the higher order characters where UTF-16 remains at just 2 bytes for most characters.
//
//    UTF-32 will cover all possible characters in 4 bytes. This makes it pretty bloated. I can't think of any advantage to using it.
//
//
//    In short:
//
//    UTF-8: Variable-width encoding, backwards compatible with ASCII. ASCII characters (U+0000 to U+007F) take 1 byte, code points U+0080 to U+07FF take 2 bytes, code points U+0800 to U+FFFF take 3 bytes, code points U+10000 to U+10FFFF take 4 bytes. Good for English text, not so good for Asian text.
//    UTF-16: Variable-width encoding. Code points U+0000 to U+FFFF take 2 bytes, code points U+10000 to U+10FFFF take 4 bytes. Bad for English text, good for Asian text.
//    UTF-32: Fixed-width encoding. All code points take four bytes. An enormous memory hog, but fast to operate on. Rarely used.
//    In long: see Wikipedia: UTF-8, UTF-16, and UTF-32.

    // so UTF-8 should suffice for our purposes

    public static final long KEEPALIVE_INCOMING_TIMEOUT = 3000; // how long without incoming messages incoming until a timeout should be triggered
    public static final long KEEPALIVE_INITIAL_BONUS = ; // is added on the first message received timeout
    public static final long KEEPALIVE_OUTGOING_TIMEOUT =;
    public static final long KEEPALIVE_MAX_PING_FLUCTUATION;// how much in positive and negative direction the ping may fluctuate at most
    public static final long KEEPALIVE_SENDING_INTERVAL = ; // how often to send a keepalive packet
        // ^this is so because we need to send early enough that the whole time it takes the packet to reach the other end is still supported
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
        // And we chose MAX_PING_FLUCTUATION, e.g. 100 ms
        // ==> SENDING_INTERVAL = INCOMING_TIMEOUT - 2*MAX_PING_FLUCTUATION, i.e for our example positive and thus possible: 2800 ms
    public static final long KEEPALIVE_
    /**
     * Internally, the {@link ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler} does the following:
     * Every time a message is sent or received, it stores the current time
     * in {@link ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler#lastInTime}
     * and {@link ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler#lastOutTime}.
     *
     * A parallel thread checks every CHECK_TIMEOUT ms whether a timeout should be triggered.
     * For this, using the Nyquist-Shannon theorem, CHECK_TIMEOUT must be at most half of INCOMING_TIMEOUT and/or SENDING_INTERVAL
     *
     **/
}
