package ch.ethz.inf.vs.a4.minker.einz.client;

import android.util.Log;

public class ClientMessengerCallback implements ClientActionCallbackInterface { // TODO: implement reactions to messages

    public ClientMessengerCallback(){

    }

    @Override
    public void onRegisterSuccess() {
        Log.d("ClientMessengerCallback", "received onRegisterSuccess");
    }
}
