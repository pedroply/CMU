package pt.ulisboa.tecnico.cmov.notp2photo;

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

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final static String APP_KEY = "jm1yrjpxz13l8ng";
    final static String APP_SECRET = "0mrpn6kv1wkclev";

    private Context context = this;
    private ListView listView;
    DbxClientV2 client;
    String token;
    String loginToken;
    String user;
    ArrayList<String> albumList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }); */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        loginToken = intent.getStringExtra("loginToken");
        user = intent.getStringExtra("user");

        if(token == null){
            Auth.startOAuth2Authentication(HomeActivity.this, APP_KEY);
        }
    }

    @Override
    protected void onResume(){
        if (token == null) {
            token = Auth.getOAuth2Token();
        }

        new albumLoader().execute();

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

        if (id == R.id.nav_viewalbums) {

        } else if (id == R.id.nav_createalbums) {
            Intent intent = new Intent(this, CreateAlbum.class);
            intent.putExtra("token", token);
            intent.putExtra("loginToken", loginToken);
            intent.putExtra("user", user);
            startActivity(intent);

        } else if (id == R.id.nav_addphoto) {

        } else if (id == R.id.nav_findusers) {

        } else if (id == R.id.nav_adduseralbum) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class albumLoader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            albumList = new ArrayList<>();
            /*DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            client = new DbxClientV2(config, token);

            // WORKS FULLY
            FullAccount account = null;
            try {
                account = client.users().getCurrentAccount();
                List<Metadata> folders = client.files().listFolder("/P2Photo").getEntries();
                for (Metadata md : folders) {
                    albumList.add(md.getName());
                }
            } catch (DbxException e) {
                e.printStackTrace();
            }*/

            // DOESN'T WORK FULLY (MIGHT NOT LIST RECENTLY ADDED ALBUM)
            String url = "http://" + WebInterface.IP + "/retriveAllAlbuns?name="+user+"&token="+loginToken;
            Log.d(MainActivity.TAG, "URL: " + url);
            String response = WebInterface.get(url);
            Log.d("Response", response);
            try {
                JSONArray mainObject = new JSONArray(response);
                for (int i = 0; i < mainObject.length(); i++) {
                    albumList.add(mainObject.get(i).toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ArrayAdapter adapter = new ArrayAdapter<String>(context,
                    R.layout.activity_home_album_view, albumList);

            listView = (ListView) findViewById(R.id.albumList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String album = (String) listView.getItemAtPosition(position);
                    Toast.makeText(context, "You selected : " + album, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(context, ViewAlbumActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("loginToken", loginToken);
                    intent.putExtra("album", album);
                    startActivity(intent);
                }
            });
        }
    }

}
