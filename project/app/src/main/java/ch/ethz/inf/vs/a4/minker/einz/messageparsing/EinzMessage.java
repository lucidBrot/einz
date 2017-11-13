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

    public EinzMessage (){

    }

    public EinzMessageHeader getHeader() {
        return header;
    }

    public void setHeader(EinzMessageHeader header) {
        this.header = header;
    }

    public EinzMessageBody getBody() {
        return body;
    }

    public void setBody(EinzMessageBody body) {
        this.body = body;
    }
}
