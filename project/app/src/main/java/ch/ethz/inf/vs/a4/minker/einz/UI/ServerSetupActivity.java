package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import ch.ethz.inf.vs.a4.minker.einz.R;


/**
 * Feel free to change this layout Chris.
 * This is to get a somewhat working app-flow as it should finally be.
 * Corresponds to screen 2 in our paper
 */
public class ServerSetupActivity extends AppCompatActivity {


    public void makeFullscreen(){
        if(getSupportActionBar() != null){
            getSupportActionBar().hide(); // might cause NullPointerException if we don't have actionBar (IntelliJ warning)
        }
        if(getActionBar() != null){
            getActionBar().hide();
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        makeFullscreen();
        //make fullscreen

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_server_setup);
        findViewById(R.id.btn_s_setupactivity_start_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLobbyAction((((EditText) findViewById(R.id.et_s_username)).getText().toString()));
            }
        });

        ((EditText) findViewById(R.id.et_s_username)).setImeActionLabel("Start Server", KeyEvent.KEYCODE_ENTER); // emulator seems to use ENTER instead of ACTION DONE
        ((EditText) findViewById(R.id.et_s_username)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_NULL
                        && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    startLobbyAction((((EditText) findViewById(R.id.et_s_username)).getText().toString()));//match this behavior to your 'Send' (or Confirm) button
                    return true;
                } // because emulator seems to ignore the setting in xml and uses ENTER

                if(actionId == EditorInfo.IME_ACTION_DONE){
                    //same thing again
                    startLobbyAction((((EditText) findViewById(R.id.et_s_username)).getText().toString()));//match this behavior to your 'Send' (or Confirm) button
                    return true; // state that we consumed this event
                }

                return false;
            }
        });
        ((EditText) findViewById(R.id.et_s_username)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                makeFullscreen();
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        makeFullscreen();
    }

    private void startLobbyAction(String username) {
        Log.d("ServerSetup", "username set to "+username);
        Intent lobbyIntent = new Intent(this, LobbyActivity.class);
        lobbyIntent.putExtra("host", true);
        String role = ((CheckBox) findViewById(R.id.cb_s_observer)).isChecked() ? "spectator" : "player";
        lobbyIntent.putExtra("username", username);
        lobbyIntent.putExtra("role", role);
        startActivity(lobbyIntent);
    }



}
