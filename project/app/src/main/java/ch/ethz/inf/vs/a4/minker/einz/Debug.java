package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.Card;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardColor;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardOrigin;
import ch.ethz.inf.vs.a4.minker.einz.model.cards.CardText;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.*;
import org.json.JSONException;
import org.json.JSONObject;

import static java.lang.Thread.sleep;

/**
 * For debugging only.
 * This class is a mess and I'm fine with that
 * function names might not correcpond to their content
 */
public class Debug {

    public static final boolean CLIENT_SLEEP_AFTER_CONNECTION_ESTABLISHED = false;
    public static final int SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED = 0; //[ms]

    // Debugging: Sony Xperia Z5 as client, Samsung galaxy tab as server, in wlan
    // Client: Sony E6653 Android 7.1.1, API 25
    // Server: Samsung GT-N5120 Android 4.4.2, API 19

    // false and 30'000     works
    // false and 0          works
    // true and 0           works
    // true and 30'000      works
    // wtf... at least the first one should have failed

    // reversing above client and server setup
    // false and 0          works
    // false and 30'000     works



    public static long a_time = 0;
    public static long a_startTime = 0;
    public static long a_endTime = 0;

    public static boolean logKeepalivePackets = false; // set to false to reduce log spam from receiving/sending keepalive packets, including the messages from actionfactory
    public static boolean logKeepaliveSpam = false; // set to false to reduce log spam from triggering maybe-timeouts and more verbose debug output
    public static boolean useKeepalive = true; // set true to use keepalive mechanisms
    public static int smallStack = -1; // set to -1 to disable
    public static boolean logRuleSpam = true;

    /**
     * called at program start in order to inform Devs about debug settings that may be unintentional
     */
    public static void debug_printInitialWarnings(){
        if(CLIENT_SLEEP_AFTER_CONNECTION_ESTABLISHED)
            Log.w("Debug", "Using deprecated CLIENT_SLEEP_AFTER_CONNECTION_ESTABLISHED = true");
        if(SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED>0)
            Log.w("Debug", "Using SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED = "+SERVER_SLEEP_AFTER_CONNECTION_ESTABLISHED);
        if(logKeepalivePackets){
            Log.w("Debug", "Log spam from keepalive packets is activated.");
        }
        if(!useKeepalive){
            Log.w("Debug", "useKeepalive is turned off.");
        } else {
            if(!logKeepaliveSpam && !logKeepalivePackets){
                Log.w("Debug", "using Keepalive but without debug logging for it.");
            }
        }
        if(logRuleSpam){
            Log.w("Debug", "using logRuleSpam, that means it will clutter the log messages with Rule registration messages serverside");
        }
    }

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
            Log.d("DEBUG", "printJSONRepresentationOF() : "+ container.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // D/DEBUG: {"test":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser"}
        //</debug>
    }

    public static void debug_simulateClient1() {
        //<DEBUG>
        final TempClient tc = new TempClient(new TempClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Log.d("TempClient1", "received message: "+message);
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
                    sleep(100); // wait until server hopefully runs
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("TempClient1", "Sleeping Failed");
                    interrupt();
                }

                tc.sendMessage(Debug.debug_getRegisterMessage("roger"));
                try {
                    sleep(600);
                } catch (InterruptedException e) {
                    Log.e("TempClient1", "Insomnia!");
                    e.printStackTrace();
                }

                //tc.sendMessage(Debug.debug_getKickMessage("clemi"));
                //tc.sendMessage(Debug.debug_getStartGameMessage());
                //tc.sendMessage(Debug.debug_getDrawCardMessage());
                //tc.sendMessage(Debug.debug_getPlayCardMessage());
                //tc.sendMessage(Debug.debug_getGetStateMessage());
                tc.sendMessage(Debug.debug_getFinishTurnMessage());
            }
        };
        m.start(); // send message
        //</Debug>
    }


    public static void debug_simulateClient2() {
        try {
            sleep(1200); // give Client1 a chance to register first
        } catch (InterruptedException e) {
            Log.w("Debug/TempClient2","Insomnia while creating client 2");
            e.printStackTrace();
        }
        //<DEBUG>
        final TempClient tc = new TempClient(new TempClient.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Log.d("TempClient2", "received message: "+message);

                /*if(message.equals("{\"header\":{\"messagegroup\":\"registration\",\"messagetype\":\"UnregisterResponse\"},\"body\":{\"username\":\"clemi\",\"reason\":\"kicked\"}}")){
                    Log.d("TempClient2", "LULZ I WAS KICKED");

                }*/
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
                    sleep(1000); // wait until server hopefully runs
                } catch (InterruptedException e) {
                    Log.e("TempClient2", "Sleeping Failed");
                    e.printStackTrace();
                    interrupt();
                }

                // tc.sendMessage(Debug.debug_getRegisterMessage("silvia"));
                tc.sendMessage(Debug.debug_getRegisterMessage("clemi"));
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    Log.e("TempClient2", "Sleeping Failed v2");
                    e.printStackTrace();
                }

                // send bad message
                EinzMessageHeader header = new EinzMessageHeader("registration", "alösdkjf");
                EinzKickFailureMessageBody badBody = new EinzKickFailureMessageBody("roger", "lol this is a bad message");
                EinzMessage<EinzKickFailureMessageBody> emsg = new EinzMessage<>(header, badBody);
                try {
                    tc.sendMessage(emsg.toJSON().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                

                tc.sendMessage(Debug.debug_getUnregisterMessage("clemi"));
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

    private static String debug_getKickMessage(String username) {
        try {
            EinzMessageHeader header = new EinzMessageHeader("registration", "Kick");
            EinzKickMessageBody body = new EinzKickMessageBody(username);
            EinzMessage<EinzKickMessageBody> message = new EinzMessage<>(header, body);
            Log.d("DEBUG/dGetUnRegMsg","simulating message.toJSON().toString() : "+message.toJSON().toString());
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("DEBUG/dGetUnRegMsg", "failed to create kick message");
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
            Log.e("DEBUG/dGetUnRegMsg", "failed to create deregistration message");
            return "empty message :(";
        }
    }


    private static String debug_getStartGameMessage() {
        try {
            EinzMessageHeader header = new EinzMessageHeader("startgame", "StartGame");
            EinzStartGameMessageBody body = new EinzStartGameMessageBody();
            EinzMessage<EinzStartGameMessageBody> message = new EinzMessage<>(header, body);
            Log.d("DEBUG/dGetUnRegMsg","simulating message.toJSON().toString() : "+message.toJSON().toString());
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("DEBUG/dGetUnRegMsg", "failed to create startGame message");
            return "empty message :(";
        }
    }

    private static String debug_getDrawCardMessage() {
        EinzMessageHeader header=new EinzMessageHeader("draw","DrawCards" );
        EinzMessageBody body  = new EinzDrawCardsMessageBody();
        EinzMessage message = new EinzMessage<>(header, body);
        try {
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "DEBUG/fuck.";
        }
    }

    private static String debug_getPlayCardMessage() {
        EinzMessageHeader header=new EinzMessageHeader("playcard","PlayCard" );
        Card card = EinzSingleton.getInstance().getCardLoader().getCardInstance("yellow_1", CardOrigin.UNSPECIFIED.value);
        EinzMessageBody body  = new EinzPlayCardMessageBody(card);
        EinzMessage message = new EinzMessage<>(header, body);
        try {
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "DEBUG/fuck.";
        }
    }

    private static String debug_getGetStateMessage() {
        EinzMessageHeader header=new EinzMessageHeader("stateinfo","GetState" );
        EinzGetStateMessageBody body = new EinzGetStateMessageBody();
        EinzMessage<EinzGetStateMessageBody> message = new EinzMessage<>(header, body);
        try {
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "DEBUG/fuck.";
        }
    }

    private static String debug_getFinishTurnMessage() {
        EinzMessageHeader header=new EinzMessageHeader("furtheractions","FinishTurn" );
        EinzFinishTurnMessageBody body = new EinzFinishTurnMessageBody();
        EinzMessage<EinzFinishTurnMessageBody> message = new EinzMessage<>(header, body);
        try {
            return message.toJSON().toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "DEBUG/fuck.";
        }
    }

    public static EinzMessage debug_getFailingKickMessage() {
        EinzMessageHeader header=new EinzMessageHeader("registration","Kick" );
        EinzKickMessageBody body = new EinzKickMessageBody("some яаndom user who should not exist, in order to trigger a kick failure message");
        EinzMessage<EinzKickMessageBody> message = new EinzMessage<>(header, body);
        return message;
    }
}
