package pt.ulisboa.tecnico.cmov.p2photo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class P2PActivity extends AppCompatActivity {

    private IntentFilter intentFilter;
    private WifiP2pManager.Channel channel;
    private WifiManager wifiManager;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;
    private List<WifiP2pDevice> peers;
    private boolean isGroupOwner = false;

    private int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2_p);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        peers = new ArrayList<WifiP2pDevice>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, this.PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }

        Button discoverButton = (Button) findViewById(R.id.discoverButton);
        Button closeButton = (Button) findViewById(R.id.closeButton);
        Button sendButton = (Button) findViewById(R.id.sendButton);
        discoverButton.setEnabled(false);
        closeButton.setEnabled(false);
        sendButton.setEnabled(false);

        if(wifiManager.isWifiEnabled()){
            Button wiFiButton = (Button) findViewById(R.id.wiFiButton);
            wiFiButton.setText("WiFi ON");
            discoverButton.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            TextView statusText = (TextView) findViewById(R.id.statusText);
            statusText.setText("Turn on your location");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            WifiP2pDevice[] peersArray = new WifiP2pDevice[peerList.getDeviceList().size()];
            peersArray = peerList.getDeviceList().toArray(peersArray);
            List<WifiP2pDevice> refreshedPeers = Arrays.asList(peersArray);
            String[] namesArray = new String[refreshedPeers.size()];

            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);
                for (int i = 0; i < peers.size(); i++) {
                    namesArray[i] = peers.get(i).deviceName;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), android.R.layout.simple_list_item_1, namesArray);
                ListView peerListView = (ListView) findViewById(R.id.peerList);
                peerListView.setAdapter(adapter);

                peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        final WifiP2pDevice device = peers.get(i);
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;

                        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                               TextView statusText = (TextView) findViewById(R.id.statusText);
                               statusText.setText("Connected to " + device.deviceName);
                            }

                            @Override
                            public void onFailure(int i) {
                                TextView statusText = (TextView) findViewById(R.id.statusText);
                                statusText.setText("Could not connect to " + device.deviceName);
                            }
                        });
                    }
                });
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                isGroupOwner = true;
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("You are the Group Owner");

            } else if (wifiP2pInfo.groupFormed) {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("You are a Client");
            }

            Button closeButton = (Button) findViewById(R.id.closeButton);
            Button sendButton = (Button) findViewById(R.id.sendButton);
            closeButton.setEnabled(true);
            sendButton.setEnabled(true);
        }
    };

    public void turnOnOffWifi(View v){
        Button wiFiButton = (Button) findViewById(R.id.wiFiButton);
        Button discoverButton = (Button) findViewById(R.id.discoverButton);

        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            wiFiButton.setText("WiFi OFF");
            discoverButton.setEnabled(false);
        }
        else {
            wifiManager.setWifiEnabled(true);
            wiFiButton.setText("WiFi ON");
            discoverButton.setEnabled(true);
        }
    }


    public void discoverPeers(View v) {
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("Discovery Started");
            }

            @Override
            public void onFailure(int reasonCode) {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("Could not start discovery");
            }
        });
    }

    public void closeConnection(View v){
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("Disconnected from group");

                Button closeButton = (Button) findViewById(R.id.closeButton);
                Button sendButton = (Button) findViewById(R.id.sendButton);
                sendButton.setEnabled(false);
                closeButton.setEnabled(false);
            }

            @Override
            public void onFailure(int reason) {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("Could not disconnect from group");

                Button closeButton = (Button) findViewById(R.id.closeButton);
                Button sendButton = (Button) findViewById(R.id.sendButton);
                sendButton.setEnabled(false);
                closeButton.setEnabled(false);
            }
        });
    }

    public void sendFiles(View v){
        if(isGroupOwner){

        } else {

        }
    }
}
