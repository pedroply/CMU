package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final static String APP_KEY = "jm1yrjpxz13l8ng";
    private Context context = this;
    private String token, loginToken, user;
    private GlobalClass global;

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
        token = global.getUserAccessToken();

        if(token == null){
            Auth.startOAuth2Authentication(HomeActivity.this, APP_KEY);
        }
    }

    @Override
    protected void onResume(){
        if (token == null) {
            token = Auth.getOAuth2Token();
            global.setUserAccessToken(token);
        }

        if(token != null){
            if(global.albumListEmpty()){
                new albumLoader().execute();
            } else {
                setAlbumList(global.getAlbumList());
            }
        }

        super.onResume();
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
        DownloadPhotosService.clearAlbums();
        ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(null);
        new albumLoader().execute();
    }

    private class albumLoader extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> albumList = new ArrayList<>();
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            DbxClientV2 client = new DbxClientV2(config, token);

            String url = "http://" + WebInterface.IP + "/retriveAllAlbuns?name=" + user + "&token=" + loginToken;
            String response = WebInterface.get(url);
            Log.i(MainActivity.TAG, "Albuns: " + response);

            try {
                // Gather all albuns in Dropbox
                List<Metadata> folders = client.files().listFolder("/P2Photo").getEntries();
                if (!folders.isEmpty()) {
                    for (Metadata md : folders) {
                        albumList.add(md.getName());
                    }
                }

                // Check all albuns in server. If they are not in Dropbox, add them
                JSONArray mainObject = new JSONArray(response);
                for(int i = 0; i < mainObject.length(); i++) {
                    String albumName = mainObject.getString(i);

                    if (!albumList.contains(albumName)) {
                        albumList.add(albumName);
                        // Carbon copy from create album
                        client.files().createFolder("/P2Photo/" + albumName);

                        String catalogPath = "/P2Photo/" + albumName + "/index.txt";
                        InputStream targetStream = new ByteArrayInputStream("".getBytes());
                        client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);

                        SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                        url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + loginToken + "&album=" + albumName;
                        WebInterface.post(url, linkMetadata.getUrl());
                    }
                }
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            global.addUserAlbums(albumList);
            return albumList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {
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

}
