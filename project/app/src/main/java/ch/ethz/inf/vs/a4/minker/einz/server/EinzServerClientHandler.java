package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
 * This class handles one Connection per instance (thread)
 */
public class EinzServerClientHandler implements Runnable{
    private Socket socket;
    public boolean spin = false;
    private ThreadedEinzServer papi;

    public EinzServerClientHandler(Socket clientSocket, ThreadedEinzServer papi) {
        Log.d("EinzServerThread", "started");
        this.socket = clientSocket;
        this.papi = papi;
        papi.incNumClients();
    }

    // source: https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server

    @Override
    public void run() {
        Log.d("EinzServerThread", "run() was called. Listening for messages");
        InputStream inp = null;
        BufferedReader brinp = null;
        DataOutputStream out = null;
        try {
            inp = socket.getInputStream();
            brinp = new BufferedReader(new InputStreamReader(inp));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.e("EinzServerThread", "Failed to initialize run(). Aborting");
            e.printStackTrace();
            return;
        }

        String line; // TODO: don't just echo the same thing back
        spin = true;
        while (spin) {
            try {
                line = brinp.readLine();
                if ((line == null) || line.equalsIgnoreCase("QUIT")) {
                    socket.close();
                    papi.decNumClients();
                    Log.d("EinzServerThread", "closed clientSocket");
                    return;
                } else {
                    Log.d("EinzServerThread", "received line: "+line);
                    parseMessage(line);
                    out.writeBytes(line + "\n\r");
                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("EinzServerThread", "Something Failed");
                return;
            }
        }
    }

    private void parseMessage(String message){
        // check if valid JSON Object
        JSONObject msg = null;
        try {
            msg = new JSONObject(message);
        } catch (JSONException e) {
            // not a valid JSON Object
            Log.w("EinzServerThread/parse", "Received message that isn't a JSON Object: "+message);
        }

        if (msg != null){ // if JSON
            try {
                switch (msg.getString("messagetype")){
                    case "play card":
                        break; // TODO: all cases
                    case "debug message":
                        Log.d("EinzServerThread/parse", "Received debug message");
                        Log.d("EinzServerThread/parse", "\twith values "+msg.getJSONArray("val").toString());
                        break;
                    default:
                        Log.w("EinzServerThread/parse", "Received JSON message without messagetype as String");
                }
            } catch (JSONException e) {
                Log.w("EinzServerThread/parse", "Valid JSON but invalid messagetype");
                //e.printStackTrace();
            }
        }
    }
}
