package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;

import java.util.ArrayList;

/**
 * Lobby List. corresponds to screen 3 in our proposal
 */
public class LobbyActivity extends AppCompatActivity implements LobbyUIInterface, View.OnClickListener {
    // implement some interface so that the client can update this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

    }


    @Override
    public void setLobbyList(ArrayList<String> players, ArrayList<String> spectators) {

    }

    @Override
    public void setAdmin(String username) {

    }

    @Override
    public void onClick(View view) {

    }
}
