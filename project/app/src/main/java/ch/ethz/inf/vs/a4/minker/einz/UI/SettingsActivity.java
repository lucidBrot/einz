package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.RulesContainer;

public class SettingsActivity extends FullscreenActivity implements View.OnClickListener {

    private RulesContainer rulesContainer = new RulesContainer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.btn_save_settings).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_save_settings:{
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
                EinzSingleton.getInstance().getEinzClient().getConnection().sendMessageRetryXTimes(3, rulesContainer.toMessage());
            }
        }).start();
    }

    private void saveSettings() {
        // TODO: write settings as profile (that can be loaded again after app restart) to disk?
    }


}
