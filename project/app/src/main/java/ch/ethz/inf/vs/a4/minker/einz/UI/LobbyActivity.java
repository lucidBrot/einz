package ch.ethz.inf.vs.a4.minker.einz.UI;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ch.ethz.inf.vs.a4.minker.einz.R;
import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunction;
import ch.ethz.inf.vs.a4.minker.einz.gamelogic.ServerFunctionDefinition;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterFailureMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;
import ch.ethz.inf.vs.a4.minker.einz.server.ThreadedEinzServer;
import info.whitebyte.hotspotmanager.ClientScanResult;
import info.whitebyte.hotspotmanager.FinishScanListener;
import info.whitebyte.hotspotmanager.WifiApManager;

import java.lang.reflect.Array;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import static java.lang.Thread.sleep;

/**
 * Lobby List. corresponds to screen 3 in our proposal.
 * Can be started either from the server device or from a client-only device.
 * Pass this Activity the following intent extra information:
 *  Both:
 *    "host" - boolean -     whether this device is hosting the server
 *    "username" - String -  the username the user entered
 *    "role" - String  -     Currently either "spectator" or "player"
 *  Client-only:
 *    "serverPort" - int -   on which port the server is listening
 *    "serverIP" - String -  at which IP the server is located
 */
public class LobbyActivity extends AppCompatActivity implements LobbyUIInterface, View.OnClickListener, ServerActivityCallbackInterface {
    // implement some interface so that the client can update this

    private ThreadedEinzServer server; // there should be only one
    private Thread serverThread;
    private ServerFunctionDefinition serverLogicInterface;
    private EinzClient ourClient;
    private String serverIP;
    private int serverPort;

    private HandlerThread backgroundThread = new HandlerThread("Networking"); // one background thread instead of many short-lived

    private boolean host; // if this device is hosting the server
    private String username;
    private String role;
    private String adminUsername; // which user was chosen as admin by the server
    private Looper backgroundLooper;
    private Handler backgroundHandler; // use this to schedule background tasks
    // TODO: what if the host is not the first user to connect? stop server and restart?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();
        this.host = intent.getBooleanExtra("host", false);
        this.username = intent.getStringExtra("username");
        this.role = intent.getStringExtra("role");

        if(this.host) {
            startServer();
            ((CardView) findViewById(R.id.cv_lobby_server_info)).setCardBackgroundColor(Color.YELLOW); // CYAN for client, Yellow for server. yey.

            // wait for server to tell us it's ready so we can connect in onLocalServerReady()
        } else {
            // still display the IP/PORT info so that they can tell their friends

            /// Option to hide the infobox
            ///((CardView) findViewById(R.id.cv_lobby_server_info)).setVisibility(View.GONE);

            // get info
            this.serverPort = intent.getIntExtra("serverPort",-1);
            this.serverIP = intent.getStringExtra("serverIP");
            // set UI to display this
            String ip = "IP: "+this.serverIP; String p = "PORT: "+String.valueOf(this.serverPort);
            ((TextView) findViewById(R.id.tv_lobby_ip)).setText(ip);
            ((TextView) findViewById(R.id.tv_lobby_port)).setText(p);
            ((CardView) findViewById(R.id.cv_lobby_server_info)).setCardBackgroundColor(Color.CYAN); // CYAN for client, Yellow for server. yey.

            // this client will only be shown in the list once the server told it that it was registered.

            // show that it is connecting
            addLobbyListUser(this.username, this.role+"   (Connecting...)");
            // this will be purged once the client receives the first UpdateLobbyList

            // start client. Because we specify "host" as false, the client will automatically register
            this.ourClient = new EinzClient(this.serverIP, this.serverPort, getApplicationContext(), this.username, this.role, false, this);
            this.ourClient.run();
        }

        ///debug_populate_lobbylist();

        // initialize background threading for small sending tasks
        this.backgroundThread.start();
        this.backgroundLooper = this.backgroundThread.getLooper();
        this.backgroundHandler = new Handler(this.backgroundLooper);


    }

    private void debug_populate_lobbylist() {

        // <Debug>
        ArrayList<String> pl = new ArrayList<>();
        pl.add("some player");
        ArrayList<String> sp = new ArrayList<>();
        sp.add("some spectator");
        pl.add("admin player");
        setAdmin("admin player");
        setLobbyList(pl, sp);
        //</Debug>
    }

    /**
     * adds the user to the list and highlights him if he's (previously set using {@link #setAdmin(String)}) admin.
     * @param username
     * @param role
     */
    private void addLobbyListUser(String username, String role) {
        LinearLayout lobbyList = findViewById(R.id.ll_lobbylist);

        CardView usercard = (CardView) LayoutInflater.from(this).inflate(R.layout.cardview_lobbylist_element, lobbyList, false);
        // false because don't add view yet - I first want to set some text

        TextView tv_username = usercard.findViewById(R.id.tv_lobbylist_username);
        TextView tv_role = usercard.findViewById(R.id.tv_lobbylist_role);

        // set text
        tv_username.setText(username);
        tv_role.setText(role);

        // highlight admin
        if(username.equals(this.adminUsername)){
            usercard.setCardBackgroundColor(Color.GREEN);
        }

        if(this.host){
            // show kick button // TODO: hide kick button for admin user
            View kickButtonFrame = usercard.findViewById(R.id.fl_lobby_kick_frame);
            kickButtonFrame.setVisibility(View.VISIBLE);
            // setup onclick listener
            Context context = this;
            View kickButton = usercard.findViewById(R.id.btn_lobby_kick);
            kickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    kick(username);
                }
            });
        }

        // add view
        lobbyList.addView(usercard);
    }

    /**
     * This method is only for admins.
     * Sends kick request in new thread
     * @param username who to kick
     */
    private void kick(String username) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ourClient.sendKickRequest(username);
            }
        };
        /* // old version
        (new Thread(r)).start();
        */
        this.backgroundHandler.post(r);
    }

    /**
     * remove all usercards from the lobby list (and all other content of the list as well)
     */
    private void clearLobbyList(){
        LinearLayout lobbyList = findViewById(R.id.ll_lobbylist);
        lobbyList.removeAllViews();
    }


    /**
     * clears the lobby list, rewrites it based on the parameters
     * @param players
     * @param spectators
     */
    @Override
    public void setLobbyList(ArrayList<String> players, ArrayList<String> spectators) {
        clearLobbyList();

        // first add all players
        for(String player : players){
            addLobbyListUser(player, "player");
        }

        // then add all spectators
        for(String spectator : spectators){
            addLobbyListUser(spectator, "spectator");
        }

    }

    @Override
    public void setAdmin(String username) {
        Log.d("LobbyActivity", "set admin to "+username);
        this.adminUsername = username;
    }

    @Override
    public void onRegistrationFailed(EinzRegisterFailureMessageBody body) {
        /*
        - "not unique" if the same username was already registered
        - "already registered" if the same connection already has registered a username
        - "invalid" if the username is the empty string or "server". Or if the username contains invalid characters. One invalid character is the Tilde, which is reserved to identify non-username-strings
        - "lobby full" if the server decided to fixate the number of players or spectators and the game has not yet started (otherwise, the server wouldn't react at all).
        - "game already in progress"
         */
        String toastMsg;
        switch (body.getReason()) {
            case "not unique":
            case "invalid":
            case "already registered":
                // make user change name
                // only the thread that created the view is allowed to update them
                toastMsg = "Please choose a different name. Yours is " + body.getReason();
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                cleanupActivity();
                this.onBackPressed();
                return;
            case "lobby full":
            case "game already in progress":
                // tell user that he cannot join
                toastMsg = "You cannot join, sorry. "+body.getReason();
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                this.onBackPressed();
                return;

            default:
                toastMsg = "Something went wrong.";
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                this.onBackPressed();
                return;

        }
    }

    @Override
    public void onClick(View view) {
        //TODO: button to start game if you're the host, handle the onclick
        //TODO: kick player buttons if you're the host
        //TODO: settings if you're the host
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cleanupActivity();
    }


    /**
     * stops server if there is one on this device. <br>
     *     stops client.
     */
    private void cleanupActivity() {
        // stop server on back button
        if(this.host && this.server!=null && !this.server.isDead()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    server.shutdown();
                    server=null;
                }
            };
            /*//old version
            (new Thread(r)).start();
            */
            // don't run this on the main thread. networking is not allowed on the main thread
            this.backgroundHandler.post(r);
        }

        if(this.ourClient!=null && !this.ourClient.isDead()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    ourClient.shutdown(true);
                    ourClient=null;
                }
            };
            //old version
            // /*(new Thread(r)).start();*/
            this.backgroundHandler.post(r);
        }
    }

    /**
     * set server serverIP and serverPort to be displayed in ui
     * @param einzServer where the IP and Port info come from
     */
    private void setIPAndPort(ThreadedEinzServer einzServer) {
        this.serverIP = getIP();
        String ip = "IP: "+this.serverIP;
        ((TextView) findViewById(R.id.tv_lobby_ip)).setText(ip);
        this.serverPort = einzServer.getPORT();
        String p = "PORT: "+String.valueOf(serverPort);
        ((TextView) findViewById(R.id.tv_lobby_port)).setText(p);

        // <Debug>
        //this.serverIP="127.0.0.1";
        //this.serverPort=8080;
        // </Debug>
    }

    private void startServer() {
        Log.d("serverSetupActivity", "startServer was pressed");
        if(serverThread==null) { // only create one server
            serverLogicInterface = new ServerFunction(); // Fabians Part
            ///server = new ThreadedEinzServer(this.getApplicationContext(), this, serverLogicInterface); // 8080 is needed for debug client. TODO: remove serverPort specification
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
        // display serverPort
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

        // if no wlan address because not connected or in hotspot mode.
        // the default ip of hotspot server would be 192.168.43.1 if the device manufacturer did not change it
        // Check if hotspot mode using https://github.com/nickrussler/Android-Wifi-Hotspot-Manager-Class

        WifiApManager wifiApManager = new WifiApManager(this);
        boolean hotspotModeOn = wifiApManager.isWifiApEnabled();
        if(hotspotModeOn){
            Toast.makeText(this, "You seem to be using a hotspot. Your IP within your own AP network is by default 192.168.43.1", Toast.LENGTH_LONG).show();
            Log.d("LobbyActivity/IP",
                    "SSID: "+wifiApManager.getWifiApConfiguration().SSID
            );
        }

        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
            NetworkInterface intf = en.nextElement();
            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                InetAddress inetAddress = enumIpAddr.nextElement();
                Log.i("LobbyActivity/IP","inetAddress.getHostAddress(): "+inetAddress.getHostAddress());

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
        this.ourClient.onServersideHandlerReady();
    }

    private void connectClientToLocalServer() {
        this.ourClient = new EinzClient(this.serverIP, this.serverPort, this.getApplicationContext(), this.username, this.role, this.host, this);
        this.ourClient.run();
        // from now on, the client has the program flow and needs to update the UI appropriately
    }
}
