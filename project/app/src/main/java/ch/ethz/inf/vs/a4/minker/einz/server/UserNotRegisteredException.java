package ch.ethz.inf.vs.a4.minker.einz.server;

public class UserNotRegisteredException extends Exception {
    public UserNotRegisteredException() {
        super();
    }

    public UserNotRegisteredException(String message) {
        super(message);
    }
}
