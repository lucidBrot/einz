package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.server.Debug;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final android.os.Handler mHideHandler = new android.os.Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            makeFullscreen();
        }
    };

    public void makeFullscreen(){
        getSupportActionBar().hide(); // might cause NullPointerException if we don't have actionBar (IntelliJ warning)

        getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        makeFullscreen();
        //make fullscreen
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {

                            mHideHandler.removeCallbacks(mHideRunnable);
                            mHideHandler.postDelayed(mHideRunnable, 2000);
                        }
                    }
                });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register onClick handlers
        findViewById(R.id.btn_start_client).setOnClickListener(this);
        findViewById(R.id.btn_start_server).setOnClickListener(this);
        // below: the non-debug listeners
        findViewById(R.id.btn_s_host_game).setOnClickListener(this);
        findViewById(R.id.btn_c_join_game).setOnClickListener(this);

        // log some warnings if debug variables have been set and possibly forgotten
        Debug.debug_printInitialWarnings();
    }

    public void onResume(){
        super.onResume();
        makeFullscreen();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_start_client:
                // start client
                Intent cintent = new Intent(this, PlayerActivity.class);
                startActivity(cintent);
                break;
            case R.id.btn_start_server:
                // start server
                Intent sintent = new Intent(this, ServerActivity.class);
                startActivity(sintent);
                break;

            case R.id.btn_s_host_game:
                // actually start the UI intended for the admin, and the server then
                Intent sIntent = new Intent(this, ServerSetupActivity.class);
                startActivity(sIntent);
                break;
            case R.id.btn_c_join_game:
                // start the UI intended for the clients
                Intent cIntent = new Intent(this, ClientSetupActivity.class);
                startActivity(cIntent);
                break;

            default:
                toast("unexpected onclick");
                Log.d("MainActivity/OnClick", "Unhandled onClick event.");
                break;
        }
    }


    /**
     * Generates a generic toast with <i>msg</i> as message and this Activity as context
     */
    private void toast(String msg){
        Context context = getApplicationContext();
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
