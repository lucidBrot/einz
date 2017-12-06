package ch.ethz.inf.vs.a4.minker.einz.keepalive;

import ch.ethz.inf.vs.a4.minker.einz.client.SendMessageFailureException;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

public interface SendMessageCallback {
    public void sendMessage(String message) throws SendMessageFailureException;
    public void sendMessage(EinzMessage<? extends EinzMessageBody> message) throws SendMessageFailureException;
}
