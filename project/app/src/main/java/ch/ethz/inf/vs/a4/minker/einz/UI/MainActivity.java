package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import ch.ethz.inf.vs.a4.minker.einz.CardLoader;
import ch.ethz.inf.vs.a4.minker.einz.EinzSingleton;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.RuleLoader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.InvalidResourceFormatException;
import ch.ethz.inf.vs.a4.minker.einz.server.Debug_ServerActivity;
import org.json.JSONArray;
import org.json.JSONException;

public class MainActivity extends FullscreenActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register onClick handlers
        //findViewById(R.id.btn_start_client).setOnClickListener(this);
        //findViewById(R.id.btn_start_server).setOnClickListener(this);
        // below: the non-debug listeners
        findViewById(R.id.btn_s_host_game).setOnClickListener(this);
        findViewById(R.id.btn_c_join_game).setOnClickListener(this);

        // log some warnings if debug variables have been set and possibly forgotten
        Debug.debug_printInitialWarnings();

        // initialize the globally available CardLoader with the cards
        initializeCardLoader();
        initializeRuleLoader();
    }

    private void initializeRuleLoader() {
        RuleLoader loader = EinzSingleton.getInstance().getRuleLoader();
        try {
            loader.loadRulesFromResourceFile(this, R.raw.rule_registration);
        } catch (JSONException e) {
            Log.e("MainActivity", "Failed to initialize RuleLoader.");
            e.printStackTrace();
        }
    }

    private void initializeCardLoader() {
        CardLoader loader = EinzSingleton.getInstance().getCardLoader();
        try {
            loader.loadCardsFromResourceFile(this, R.raw.card_definition);
        } catch (JSONException e) {
            Log.e("MainActivity", "Failed to initialize CardLoader.");
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
//            case R.id.btn_start_client:
//                // start client
//                Intent cintent = new Intent(this, PlayerActivity.class);
//                startActivity(cintent);
//                break;
//            case R.id.btn_start_server:
//                // start server
//                Intent sintent = new Intent(this, Debug_ServerActivity.class);
//                startActivity(sintent);
//                break;

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
