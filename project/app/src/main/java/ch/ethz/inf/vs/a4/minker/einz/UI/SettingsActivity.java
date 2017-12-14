package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.RulesContainer;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;

import java.util.ArrayList;

public class SettingsActivity extends FullscreenActivity implements View.OnClickListener, LobbyUIInterface {

    private RulesContainer rulesContainer = new RulesContainer();
    private EinzClient ourClient = EinzSingleton.getInstance().getEinzClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.btn_save_settings).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save_settings: {
                saveAndSend();
                onBackPressed();
                break;
            }
        }
    }

    private void saveAndSend() {
        saveSettings();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ourClient.getConnection().sendMessageRetryXTimes(3, rulesContainer.toMessage());
            }
        }).start();
    }

    private void saveSettings() {
        // TODO: write settings as profile (that can be loaded again after app restart) to disk?
    }


    @Override
    public void setLobbyList(ArrayList<String> players, ArrayList<String> spectators) {
        Log.d("TEMP", "users: "+players.get(players.size()-1));
    }

    @Override
    public void setAdmin(String username) {

    }

    @Override
    public void onRegistrationFailed(EinzRegisterFailureMessageBody body) {

    }

    @Override
    public void startGameUIWithThisAsContext() {

    }

    @Override
    public void onKeepaliveTimeout() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.ourClient != null && this.ourClient.getActionCallbackInterface() != null) {
            this.ourClient.getActionCallbackInterface().setLobbyUI(null); // make sure no callbacks to this activity are executed
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (ourClient != null && ourClient.getActionCallbackInterface() != null)
            this.ourClient.getActionCallbackInterface().setLobbyUI(this);
    }
}
