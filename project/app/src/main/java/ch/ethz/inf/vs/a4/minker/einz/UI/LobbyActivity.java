package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Intent;
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
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClientConnection;
import ch.ethz.inf.vs.a4.minker.einz.client.TempClient;
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

import static java.lang.Thread.sleep;

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

    private boolean host; // if this device is hosting the server
    private String username;
    private String role;
    // TODO: what if the host is not the first user to connect? stop server and restart?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        this.host = intent.getBooleanExtra("host", false);
        this.username = intent.getStringExtra("username");
        this.role = intent.getStringExtra("role");

        startServer();
        // wait for server to tell us it's ready so we can connect in onLocalServerReady()
    }


    @Override
    public void setLobbyList(ArrayList<String> players, ArrayList<String> spectators) {

    }

    @Override
    public void setAdmin(String username) {
        // make the corresponding name red or something
    }

    @Override
    public void onClick(View view) {
        //TODO: button to start game if you're the host, handle the onclick
        //TODO: kick player buttons if you're the host
        //TODO: settings if you're the host
    }

    /**
     * set server ip and port to be displayed in ui
     * @param einzServer
     */
    private void setIPAndPort(ThreadedEinzServer einzServer) {
        this.ip = getIP();
        String ip = "IP: "+this.ip;
        ((TextView) findViewById(R.id.tv_lobby_ip)).setText(ip);
        this.port = einzServer.getPORT();
        String p = "PORT: "+String.valueOf(port);
        ((TextView) findViewById(R.id.tv_lobby_port)).setText(p);

        // <Debug>
        //this.ip="127.0.0.1";
        //this.port=8080;
        // </Debug>
    }

    private void startServer() {
        Log.d("serverSetupActivity", "startServer was pressed");
        if(serverThread==null) { // only create one server
            serverLogicInterface = new ServerFunction(); // Fabians Part
            ///server = new ThreadedEinzServer(this.getApplicationContext(), this, serverLogicInterface); // 8080 is needed for debug client. TODO: remove port specification
            server = new ThreadedEinzServer(this.getApplicationContext(),8080, this, serverLogicInterface);
            setIPAndPort(server);
            server.setDEBUG_ONE_MSG(false); // set to true to let server generate messages on same host
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
        } else {
            Log.d("LobbyActivity/IP/1stTry", "wlan address: "+ip);
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
            Log.d("LobbyActivity/IP", "wlan address: "+wifiIpAddress);
            if(!wifiIpAddress.equals("0.0.0.0"))
                return wifiIpAddress;
        }

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                Log.i("LobbyActivity/IP","inetAddress.getHostAddress(): "+inetAddress.getHostAddress());
//the condition after && is missing in your snippet, checking instance of inetAddress
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                    Log.i("LobbyActivity/IP","return inetAddress.getHostAddress(): "+inetAddress.getHostAddress());
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
    //    setIPAndPort(server);
/*        try {
            sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        connectClientToLocalServer();
    }

    /**
     * When you are the host and the first client-handler in the server is ready to receive the register message
     */
    @Override
    public void onFirstESCHReady() {
        this.ourClient.sendRegistrationMessage();
    }

    private void connectClientToLocalServer() {
        this.ourClient = new EinzClient(this.ip, this.port, this.getApplicationContext(), this.username, this.role);
        this.ourClient.run();
        // from now on, the client has the program flow and needs to update the UI appropriately
    }
}
