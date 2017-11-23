package ch.ethz.inf.vs.a4.minker.einz.server;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.actiontypes.EinzUnregisterRequestAction;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

/**
 * This Activity starts the server and manages the Serverside UI
 */
public class ServerActivity extends AppCompatActivity implements View.OnClickListener, ServerActivityCallbackInterface {

    public static ThreadedEinzServer getServer() {
        return server;
    }

    private static ThreadedEinzServer server; //static because there should be only one
    private Thread serverThread;
    private ServerFunctionDefinition serverLogicInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        findViewById(R.id.btn_s_listen_for_clients).setOnClickListener(this);
        findViewById(R.id.btn_s_start_game_initialization).setOnClickListener(this);

        serverLogicInterface = new ServerFunction(); // Fabians Part

        server = new ThreadedEinzServer(this.getApplicationContext(),8080,this, serverLogicInterface); // 8080 is needed for debug client. TODO: remove port specification
        serverThread = new Thread(server);
        // run server to listen to clients only when button pressed

        // display server port
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation") // https://stackoverflow.com/a/20846328/2550406
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        if(ip.equals("0.0.0.0") || ip.equals("") || ip.equals("null")){
            // not connected via WIFI, use something else
            try {
                ip=getLocalIpAddress(); // use the code of some stackoverflow dude.
            } catch (SocketException e) {
                ip = e.getMessage();
                e.printStackTrace();
            }
        }
        String temp = ip + ":" + server.getPORT();
        ((TextView) findViewById(R.id.tv_s_ipport)).setText(temp);


        Debug.debug_printJSONRepresentationOf(EinzUnregisterRequestAction.class);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.btn_s_listen_for_clients:
                // start server
                if(serverThread.getState() == Thread.State.NEW) //https://developer.android.com/reference/java/lang/Thread.State.html#RUNNABLE
                    serverThread.start(); // TODO: stop server on back button
                break;
            case R.id.btn_s_start_game_initialization:
                // TODO: start game
                break;
        }
    }

    // https://stackoverflow.com/a/30183130/2550406
    public String getLocalIpAddress() throws SocketException {
        WifiManager wifiMgr = (WifiManager) this.getApplicationContext().getSystemService(getApplicationContext().WIFI_SERVICE);
        if(wifiMgr.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();
            String wifiIpAddress = String.format(Locale.US, "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
            if(!wifiIpAddress.equals("0.0.0.0"))
                return wifiIpAddress;
        }

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                Log.i("ServerActivity/IP","inetAddress.getHostAddress(): "+inetAddress.getHostAddress());
//the condition after && is missing in your snippet, checking instance of inetAddress
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    Log.i("ServerActivity/IP","return inetAddress.getHostAddress(): "+inetAddress.getHostAddress());
                    return inetAddress.getHostAddress();
                }

            }
        }

        return null;
    }

    /**
     * Updates the UI from within the UI thread
     * @param num new number of Clients connected
     */
    @Override
    public void updateNumClientsUI(final int num) {
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                ((TextView) findViewById(R.id.tv_num_connected_clients)).setText(new String("#Connected clients: "+num));
            }
        });
    }
}
