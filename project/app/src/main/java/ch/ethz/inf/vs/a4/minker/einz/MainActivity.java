package ch.ethz.inf.vs.a4.minker.einz;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import ch.ethz.inf.vs.a4.minker.einz.client.ClientActivity;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // register onClick handlers
        findViewById(R.id.btn_start_client).setOnClickListener(this);
        findViewById(R.id.btn_start_server).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_start_client:
                // start client
                Intent cintent = new Intent(this, ClientActivity.class);
                startActivity(cintent);
                break;
            case R.id.btn_start_server:
                // start server
                Intent sintent = new Intent(this, ServerActivity.class);
                startActivity(sintent);
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
