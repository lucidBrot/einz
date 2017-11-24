package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.client.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

/**
 * Lobby List. corresponds to screen 3 in our proposal
 */
public class LobbyActivity extends AppCompatActivity implements LobbyUIInterface, View.OnClickListener, ServerActivityCallbackInterface {
    // implement some interface so that the client can update this

    private ThreadedEinzServer server; // there should be only one
    private Thread serverThread;
    private ServerFunctionDefinition serverLogicInterface;
    private EinzClient ourClient;
    private String ip;
    private int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        startServer();
        // wait for server to tell us it's ready so we can connect in onLocalServerReady()
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

    /**
     * set server ip and port to be displayed in ui
     * @param einzServer
     */
    private void setIPAndPort(ThreadedEinzServer einzServer) {
        this.ip = "IP: "+getIP();
        ((TextView) findViewById(R.id.tv_lobby_ip)).setText(ip);
        this.port = einzServer.getPORT();
        String p = "PORT: "+String.valueOf(port);
        ((TextView) findViewById(R.id.tv_lobby_port)).setText(p);
    }

    private void startServer() {
        Log.d("serverSetupActivity", "startServer was pressed");
        if(serverThread==null) { // only create one server
            serverLogicInterface = new ServerFunction(); // Fabians Part
            server = new ThreadedEinzServer(this.getApplicationContext(), 8080, this, serverLogicInterface); // 8080 is needed for debug client. TODO: remove port specification
            // TODO: replace this with the call for an arbitrary port ( remove 8080 )
            serverThread = new Thread(server);
            serverThread.start();
        }

    }

    /**
     * @return the probably used IP address
     */
    public String getIP(){
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
        return ip;
    }

    // https://stackoverflow.com/a/30183130/2550406
    private String getLocalIpAddress() throws SocketException {
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

    @Override
    public void updateNumClientsUI(int num) {
        // don't care
    }

    @Override
    public void onLocalServerReady() {
        Log.d("LobbyActivity", "local server ready. Connecting...");
        connectClientToLocalServer();
        setIPAndPort(server);
    }

    private void connectClientToLocalServer() {
        this.ourClient = new EinzClient(this.ip, this.port);
    }
}
