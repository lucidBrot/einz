package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class FullscreenActivity extends AppCompatActivity {
    private final android.os.Handler mHideHandler = new android.os.Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            makeFullscreen();
        }
    };



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
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY); // make sticky
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        makeFullscreen();

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            mHideHandler.removeCallbacks(mHideRunnable);
                            mHideHandler.postDelayed(mHideRunnable, 1000);
                        }
                    }
                });
        super.onCreate(savedInstanceState);
    }

    public void onResume(){
        super.onResume();
        makeFullscreen();
    }
}
