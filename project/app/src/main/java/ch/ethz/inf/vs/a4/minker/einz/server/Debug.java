package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import org.json.JSONException;
import org.json.JSONObject;

public class Debug {

    /**
     * For debug purposes only, should not have side effects at all.
     * Prints the class of the given object as JSON
     * @param o
     */
    public static void debug_printJSONRepresentationOf(Object o){
        //<debug>
        JSONObject container = new JSONObject();
        try {
            container.put("your Object:", o);
            Log.d("ESCH/DEBUG", "printJSONRepresentationOF() : "+ container.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // D/DEBUG: {"test":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser"}
        //</debug>
    }

    public static void debug_simulateRegisterClient() {
        //<DEBUG>
        final TempClient tc = new TempClient(new TempClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Log.d("TempClient", "received message "+message);
            }
        });
        Thread t = new Thread(){
            @Override
            public void run() {
                // DEBUG: start client
                // temporary. please do not use in real code
                Log.d("EinzServer->TempClient", "simulating client");

                tc.run();
            }
        };
        t.start(); // start client stub for debug
        Thread m = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1); // wait until server hopefully runs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("TempClient", "Sleeping Failed");
                    interrupt();
                }

                tc.sendMessage(Debug.debug_getRegisterMessage("roger"));
            }
        };
        m.start(); // send message
        //</Debug>
    }

    public static void debug_simulateRegisterFollowedByUnregister() {
        //<DEBUG>
        final TempClient tc = new TempClient(new TempClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Log.d("TempClient2", "received message "+message);
            }
        });
        Thread t = new Thread(){
            @Override
            public void run() {
                // DEBUG: start client
                // temporary. please do not use in real code
                Log.d("EinzServer->TempClient2", "simulating client");

                tc.run();
            }
        };
        t.start(); // start client stub for debug
        Thread m = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(600); // wait until server hopefully runs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("TempClient2", "Sleeping Failed");
                    interrupt();
                }

                tc.sendMessage(Debug.debug_getRegisterMessage("clemï"));
                tc.sendMessage(Debug.debug_getUnregisterMessage("clemï"));
            }
        };
        m.start(); // send message
        //</Debug>
    }

    public static String debug_getRegisterMessage(String username){
        /*
        {
          "header":{
            "messagegroup":"registration",
            "messagetype":"Register"
          },
          "body":{
            "username":"roger",
            "role":"player"
          }
        }
         */


        try {
            EinzMessageHeader header = new EinzMessageHeader("registration", "Register");
            EinzRegisterMessageBody body = new EinzRegisterMessageBody(username, "player");
            EinzMessage<EinzRegisterMessageBody> message = new EinzMessage<>(header, body);
            Log.d("DEBUG/dGetRegMsg","simulating message.toJSON().toString() : "+message.toJSON().toString());
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("DEBUG/dGetRegMsg", "failed to create deregistration message");
            return "empty message :(";
        }

    }

    public static String debug_getUnregisterMessage(String username){
        try {
            EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterRequest");
            EinzUnregisterRequestMessageBody body = new EinzUnregisterRequestMessageBody(username);
            EinzMessage<EinzUnregisterRequestMessageBody> message = new EinzMessage<>(header, body);
            Log.d("DEBUG/dGetUnRegMsg","simulating message.toJSON().toString() : "+message.toJSON().toString());
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("DEBUG/dGetUnRegMsg", "failed to create registration message");
            return "empty message :(";
        }
    }

}
