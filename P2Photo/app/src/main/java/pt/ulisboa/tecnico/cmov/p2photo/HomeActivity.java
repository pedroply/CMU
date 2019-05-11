package pt.ulisboa.tecnico.cmov.p2photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = this;
    String loginToken;
    String user;
    private IntentFilter intentFilter;
    private WifiP2pManager.Channel channel;
    private WifiManager wifiManager;
    private WifiP2pInfo p2pInfo;
    private WifiP2pManager manager;
    private BroadcastReceiver receiver;
    private List<WifiP2pDevice> peers;
    private static WifiP2pDevice myDevice;
    private GlobalClass global;
    private boolean isGroupOwner = false;
    private WifiP2pServiceRequest request = null;

    private int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1001;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        global = (GlobalClass) getApplicationContext();
        user = global.getUserName();
        loginToken = global.getUserLoginToken();

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

        startRegistration();
        manager.setDnsSdResponseListeners(channel, servListener, txtListener);
        setRepeatingAsyncTask();
    }

    @Override
    protected void onResume(){
        if(global.albumListEmpty()){
            new albumLoader().execute();
        } else {
            setAlbumList(global.getAlbumList());
        }

        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @SuppressLint("NewApi")
    private void serviceRequest(){
        if(request != null){
            manager.removeServiceRequest(channel, request, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    addServiceRequest();
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), "Could not remove service request", Toast.LENGTH_SHORT).show();
                }
            });
        } else  {
            addServiceRequest();
        }
    }

    @SuppressLint("NewApi")
    private void addServiceRequest(){
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        request = serviceRequest;
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
        manager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Map record = new HashMap();
                record.put("available", "visible");

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

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Could not clear local services", Toast.LENGTH_SHORT).show();
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

                String myName = myDevice.deviceName;

                if(srcDevice.deviceName.compareTo(myName) < 0){
                    manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(), "Connected to " + srcDevice.deviceName, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i) {
                            Toast.makeText(getApplicationContext(), "Could not connect to " + srcDevice.deviceName, Toast.LENGTH_SHORT).show();
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

    WifiP2pManager.ConnectionInfoListener connectionListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                isGroupOwner = true;
                Toast.makeText(getApplicationContext(), "You are the group owner", Toast.LENGTH_SHORT).show();

                if(global.getServerUploadSocket() == null) {
                    new HomeActivity.startUploadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if(global.uploadSocketsAreClosed()){
                    new HomeActivity.startUploadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

                if(global.getServerDownloadSocket() == null){
                    new HomeActivity.startDownloadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else if(global.downloadSocketsAreClosed()){
                    new HomeActivity.startDownloadSocketAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else if (wifiP2pInfo.groupFormed) {
                Toast.makeText(getApplicationContext(), "You are the client", Toast.LENGTH_SHORT).show();
                global.resetSockets();
            }

            p2pInfo = wifiP2pInfo;
            sendFiles();
        }
    };

    @SuppressLint("NewApi")
    public void discoverPeers() {
        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Discovery started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Could not start discovery", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void closeConnection(){
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Closed Connection", Toast.LENGTH_SHORT).show();

                if(isGroupOwner){
                    try{
                        global.closeDownloadSockets();
                        global.closeUploadSockets();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Connection already closed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sendFiles(){
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
            ServerFileService.setActivity(this);
            startService(intent);

        } else {
            Intent intent = new Intent(this, ClientFileService.class);
            intent.putExtra("host", p2pInfo.groupOwnerAddress.getHostAddress());
            ClientFileService.setActivity(this);
            startService(intent);
        }

    }

    public static void setMyDevice(WifiP2pDevice device){
        myDevice = device;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_createalbums) {
            Intent intent = new Intent(this, CreateAlbum.class);
            startActivity(intent);

        } else if (id == R.id.nav_addphoto) {
            Intent intent = new Intent(this, ChooseAlbumActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_findusers) {
            Intent intent = new Intent(this, UserListActivity.class);
            startActivity(intent);

        } else if(id == R.id.nav_eventlog) {
            Intent intent = new Intent(this, LogActivity.class);
            startActivity(intent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void refreshAlbums(View v)
    {
        global.clearDownloads();
        ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(null);
        new albumLoader().execute();
    }

    private ArrayList<String> listDirectory(){
        File file = this.getFilesDir();
        File[] list = file.listFiles();
        ArrayList<String> titles = new ArrayList<String>();

        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                titles.add(list[i].getName());
            }
        }
        return titles;
    }

    private class albumLoader extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> listFolders = listDirectory();

            String url = "http://" + WebInterface.IP + "/retriveAllAlbuns?name=" + user + "&token=" + loginToken;
            String response = WebInterface.get(url);
            if(response == null)
                return null;
            Log.i(MainActivity.TAG, "Albuns: " + response);

            try {
                // Check all albuns in server. If they are not in Dropbox, add them
                JSONArray mainObject = new JSONArray(response);
                for(int i = 0; i < mainObject.length(); i++) {
                    String albumName = mainObject.getString(i);

                    if (!listFolders.contains(albumName)) {
                        listFolders.add(albumName);

                        File album = new File( context.getFilesDir() + "/" + albumName);
                        album.mkdir();

                        File indexFile = new File(context.getFilesDir() + "/" + albumName + "/" + "index.txt");
                        indexFile.createNewFile();

                    } else {
                        File file = new File(getApplicationContext().getFilesDir() + "/" + albumName);
                        File[] list = file.listFiles();
                        TreeMap<String,Bitmap> photos = new TreeMap<String, Bitmap>();

                        for (int j = 0; j < list.length; j++) {
                            if (list[j].isFile() && !list[j].getName().contains("index.txt")) {

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                Bitmap bitmap = BitmapFactory.decodeFile(list[j].getPath(), options);
                                photos.put(list[j].getPath(), bitmap);
                            }
                        }

                        global.addNewAlbum(albumName);
                        global.addPhotosToAlbum(albumName, photos);

                    }

                    url = "http://" + WebInterface.IP + "/retriveAlbum?name=" + user + "&token=" + loginToken + "&album=" + albumName;
                    response = WebInterface.get(url);
                    JSONObject mainObjectJSON = new JSONObject(response);
                    JSONArray linkArray = mainObjectJSON.getJSONArray("clients");

                    global.addNewAlbumShared(albumName);
                    global.addUsersSharedWithAlbum(albumName, linkArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            }

            global.addUserAlbums(listFolders);
            return listFolders;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
            if(list == null)
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
            setAlbumList(global.getAlbumList());
        }
    }

    private void setAlbumList(ArrayList<String> list) {
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_home_album_view, list);

        final ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String album = (String) listView.getItemAtPosition(position);
                Toast.makeText(context, "You selected : " + album, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, ViewAlbumActivity.class);
                intent.putExtra("album", album);
                startActivity(intent);
            }
        });
    }

    public void deleteAllFolders(View v){
        File file = this.getFilesDir();
        File[] list = file.listFiles();

        for (int i = 0; i < list.length; i++) {
            if (list[i].isDirectory()) {
                File[] files = list[i].listFiles();

                for(int j = 0; j < files.length; j++){
                    files[j].delete();
                }

                list[i].delete();
            }
        }
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

    private void setRepeatingAsyncTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        serviceRequest();
                        discoverPeers();
                    }
                });
            }
        };

        timer.schedule(task, 0, 60*1000);  // interval of one minute

    }

}
