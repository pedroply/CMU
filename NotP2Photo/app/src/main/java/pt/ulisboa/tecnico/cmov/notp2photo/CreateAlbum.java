package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.FolderSharingInfo;
import com.dropbox.core.v2.sharing.PathLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.users.FullAccount;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CreateAlbum extends AppCompatActivity {

    DbxClientV2 client;
    String accessToken = "", token;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        Intent intent = getIntent();
        accessToken = intent.getStringExtra("token");
        token = intent.getStringExtra("loginToken");
        user = intent.getStringExtra("user");

    }

    public void createAlbum(View v){
        EditText albumNameText = findViewById(R.id.editAlbumName);
        String albumName = albumNameText.getText().toString();

        new CreateAlbumTask().execute(albumName);
    }

    class CreateAlbumTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute(){
        }

        protected String doInBackground(String... path) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            Log.i(MainActivity.TAG, accessToken);
            client = new DbxClientV2(config, accessToken);

            try {
                // Create folder in dropbox
                client.files().createFolder("/P2Photo/" + path[0]);

                // TODO: DO SOMETHING WITH RESPONSES
                /* String url = "http://" + WebInterface.IP + "/createAlbum?name="+user+"&token="+token+"&album="+path[0];
                Log.d(MainActivity.TAG, "URL: " + url);
                String response = WebInterface.get(url); */

                // Create new blank file in the created folder and put its link in the server
                String catalogPath = "/P2Photo/" + path[0] + "/index.txt";
                InputStream targetStream = new ByteArrayInputStream("".getBytes());
                client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);
                SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                String url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + token + "&album" + path[0];
                WebInterface.post(url, linkMetadata.getUrl());
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return path[0];
        }

        @Override
        protected void onPostExecute(String response) {
            if(response != null){
                Toast toast = Toast.makeText(getApplicationContext(), "New Album Created!", Toast.LENGTH_SHORT);
                toast.show();
            }
            finish();
        }
    }


}
