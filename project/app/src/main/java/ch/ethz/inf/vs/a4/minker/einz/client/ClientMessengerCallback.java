package ch.ethz.inf.vs.a4.minker.einz.client;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterSuccessMessageBody;

public class ClientMessengerCallback implements ClientActionCallbackInterface { // TODO: implement reactions to messages

    public ClientMessengerCallback(){

    }

    @Override
    public void onRegisterSuccess(EinzMessage<EinzRegisterSuccessMessageBody> message) {
        Log.d("ClientMessengerCallback", "received onRegisterSuccess");
    }
}
