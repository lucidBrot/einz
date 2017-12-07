package ch.ethz.inf.vs.a4.minker.einz;

/**
 * origin can be any of these or a username
 */
public enum CardOrigin {
    UNSPECIFIED("~unspecified"),
    STACK("~stack"),
    TALON("~talon");

    public String value;
    CardOrigin(String origin){this.value = origin;}
}
