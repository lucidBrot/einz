package ch.ethz.inf.vs.a4.minker.einz.messageparsing;

/**
 * Immutable container of an {@link EinzMessageHeader} and an {@link EinzMessageBody}
 */
public class EinzMessage {
    private EinzMessageHeader header;
    private EinzMessageBody body;

    /**
     * Create a <i>EinzMessage</i> Object that consists of a Header and a Body
     * @param header Uniform among all messages
     * @param body Specific to the messagetype
     */
    public EinzMessage (EinzMessageHeader header, EinzMessageBody body){
        this.header = header;
        this.body = body;
    }

    public EinzMessageHeader getHeader() {
        return header;
    }

    public EinzMessageBody getBody() {
        return body;
    }
}
