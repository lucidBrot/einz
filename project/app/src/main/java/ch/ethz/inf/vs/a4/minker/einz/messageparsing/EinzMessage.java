package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

public class EinzMessage {
    public EinzMessageHeader header;
    public EinzMessageBody body;

    /**
     * Create a <i>EinzMessage</i> Object that consists of a Header and a Body
     * @param header Uniform among all messages
     * @param body Specific to the messagetype
     */
    public EinzMessage (EinzMessageHeader header, EinzMessageBody body){
        this.header = header;
        this.body = body;
    }
}
