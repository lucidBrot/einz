package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;

import java.io.*;
import java.net.Socket;

/**
 * This class handles one Connection per instance (thread)
 */
public class EinzServerThread implements Runnable{
    private Socket socket;
    public boolean spin = false;
    public EinzServerThread(Socket clientSocket) {
        Log.d("EinzServerThread", "started");
        this.socket = clientSocket;
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
                    Log.d("EinzServerThread", "closed clientSocket");
                    return;
                } else {
                    Log.d("EinzServerThread", "received line: "+line);

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
}
