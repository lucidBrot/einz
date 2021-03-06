package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import ch.ethz.inf.vs.a4.minker.einz.R;

import java.net.InetAddress;

public class ClientSetupActivity extends FullscreenActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_setup);

        findViewById(R.id.btn_c_setup).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_c_setup:
                openLobby();
                break;
        }
    }

    private void openLobby() {
        String username = ((EditText) findViewById(R.id.et_c_setup_username)).getText().toString();

        ((EditText) findViewById(R.id.et_c_setup_username)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                makeFullscreen();
            }
        });

        String ip = ((EditText) findViewById(R.id.et_c_setup_ip)).getText().toString();

        ((EditText) findViewById(R.id.et_c_setup_ip)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                makeFullscreen();
            }
        });

        if(!Patterns.IP_ADDRESS.matcher(ip).matches()){
            Toast toast = Toast.makeText(this, "Bad IP address", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        String port_ = ((EditText) findViewById(R.id.et_c_setup_port)).getText().toString();
        ((EditText) findViewById(R.id.et_c_setup_port)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                makeFullscreen();
            }
        });

        int port = -1;
        try{
            port = Integer.valueOf(port_);
        } catch (Exception e){
            Toast.makeText(this, "Bad Port", Toast.LENGTH_SHORT).show();
            return;
        }


        String role = ((CheckBox) findViewById(R.id.cb_c_spectator)).isChecked() ? "spectator" : "player";
        Intent lobbyIntent = new Intent(this, LobbyActivity.class);
        lobbyIntent.putExtra("host", false);
        lobbyIntent.putExtra("role", role);
        lobbyIntent.putExtra("serverPort", port);
        lobbyIntent.putExtra("serverIP", ip);
        lobbyIntent.putExtra("username", username);

        startActivity(lobbyIntent);
    }
}
