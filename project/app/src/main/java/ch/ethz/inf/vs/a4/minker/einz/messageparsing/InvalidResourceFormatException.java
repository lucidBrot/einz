package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

public class InvalidResourceFormatException extends Exception {

    private String extendedmessage;

    public InvalidResourceFormatException(){
        super();
        this.extendedmessage = getMessage();
        if(this.extendedmessage==null) this.extendedmessage = "";
    }

    /**
     * @param message will be prepended to the existing message
     */
    public void extendMessage(String message){this.extendedmessage = message + "\n" + this.getMessage();}

    /**
     * @param message the message to prepend
     * @return this exception object such that it can be reused inline
     */
    public InvalidResourceFormatException extendMessageInline(String message){
        this.extendedmessage = message + "\n" + this.getMessage();
        return this;}

    public String getExtendedMessage(){return this.extendedmessage;}

}
