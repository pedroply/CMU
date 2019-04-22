package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = this;
    String loginToken;
    String user;
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
    }

    @Override
    protected void onResume(){
        if(global.albumListEmpty()){
            new albumLoader().execute();
        } else {
            setAlbumList(global.getAlbumList());
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

        } else if(id == R.id.nav_findPeers) {
            Intent intent = new Intent(this, P2PActivity.class);
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

                    }
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
