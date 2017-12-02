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
}
