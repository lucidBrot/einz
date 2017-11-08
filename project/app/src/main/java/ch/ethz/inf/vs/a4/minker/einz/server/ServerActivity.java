package ch.ethz.inf.vs.a4.minker.einz.server;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.R;

/**
 * This Activity starts the server and manages the Serverside UI
 */
public class ServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        // start server
        // TODO: Serverside UI: start server on buttonpress
        // TODO: set Server Port from UI
        ThreadedEinzServer server = new ThreadedEinzServer(8080);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }
}
