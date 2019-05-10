package pt.ulisboa.tecnico.cmov.p2photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2PActivity extends AppCompatActivity {

    private GlobalClass global;
    private IntentFilter intentFilter;
    private WifiP2pManager.Channel channel;
    private WifiManager wifiManager;
    private WifiP2pInfo p2pInfo;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;
    private List<WifiP2pDevice> peers;
    private boolean isGroupOwner = false;

    private int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;

    @SuppressLint("NewApi")
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

        TextView statusText = (TextView) findViewById(R.id.statusText);
        statusText.setText("Turn on your location");

        global = (GlobalClass) getApplicationContext();

        startRegistration();
        manager.setDnsSdResponseListeners(channel, servListener, txtListener);
        addServiceRequest();
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

    @SuppressLint("NewApi")
    private void addServiceRequest(){
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Created service request", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Could not create service request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NewApi")
    private void startRegistration(){
        Map record = new HashMap();
        record.put("available", "visible");
        record.put("buddy", "Ola");

        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("P2Photo", "_presence._tcp", record);

        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Registered Service", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Could not register service", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NewApi")
    WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
        @Override
        public void onDnsSdServiceAvailable(String instanceName, String registrationType, final WifiP2pDevice srcDevice) {
            if(instanceName.equals("P2Photo")){
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = srcDevice.deviceAddress;

                String myName = Build.MODEL;
                if(srcDevice.deviceName.compareTo(myName) < 0){
                    manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            TextView statusText = (TextView) findViewById(R.id.statusText);
                            statusText.setText("Connected to " + srcDevice.deviceName);
                        }

                        @Override
                        public void onFailure(int i) {
                            TextView statusText = (TextView) findViewById(R.id.statusText);
                            statusText.setText("Could not connect to " + srcDevice.deviceName);
                        }
                    });
                }

            }
        }
    };

    @SuppressLint("NewApi")
    WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
        @Override
        public void onDnsSdTxtRecordAvailable(String fullDomain, Map record, WifiP2pDevice device) {
            Log.d(MainActivity.TAG, record.toString());
        }
    };

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

                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(), R.layout.activity_user_list_view, namesArray);
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

                if(global.getServerUploadSocket() == null) {
                    new startUploadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if(global.uploadSocketsAreClosed()){
                    new startUploadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                if(global.getServerDownloadSocket() == null){
                    new startDownloadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if(global.downloadSocketsAreClosed()){
                    new startDownloadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else if (wifiP2pInfo.groupFormed) {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("You are a Client");
                global.resetSockets();
            }

            Button closeButton = (Button) findViewById(R.id.closeButton);
            Button sendButton = (Button) findViewById(R.id.sendButton);
            closeButton.setEnabled(true);
            sendButton.setEnabled(true);

            p2pInfo = wifiP2pInfo;
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

    @SuppressLint("NewApi")
    public void discoverPeers(View v) {
        /*manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {

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
        });*/

        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("Discovery Started");
            }

            @Override
            public void onFailure(int reason) {
                TextView statusText = (TextView) findViewById(R.id.statusText);
                statusText.setText("Could not start discovery" + reason);

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

                if(isGroupOwner){
                    try{
                        global.closeDownloadSockets();
                        global.closeUploadSockets();
                    } catch(IOException e){
                        e.printStackTrace();
                        statusText.setText("Could not close sockets");
                    }

                }
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
            //TODO: add a timeout
            while(global.getClientDownloadSocket() == null || global.getClientUploadSocket() == null){
            //while(global.getClientUploadSocket() == null){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(this, ServerFileService.class);
            startService(intent);

        } else {
            Intent intent = new Intent(this, ClientFileService.class);
            intent.putExtra("host", p2pInfo.groupOwnerAddress.getHostAddress());
            startService(intent);
        }

        finish();
    }

    class startDownloadSocketAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                global.waitForClientDownloadSocket();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class startUploadSocketAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                global.waitForClientUploadSocket();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


}
