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

import org.json.JSONException;
import org.json.JSONObject;

public class CreateAlbum extends AppCompatActivity {

    DropboxAPI<AndroidAuthSession> mDBApi;
    AccessTokenPair tokens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        AppKeyPair appKeys = new AppKeyPair(HomeActivity.APP_KEY, HomeActivity.APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
    }

    @Override
    protected void onResume(){
        if(tokens == null){
            mDBApi.getSession().startAuthentication(CreateAlbum.this);
            if (mDBApi.getSession().authenticationSuccessful()) {
                try {
                    mDBApi.getSession().finishAuthentication();

                    tokens = mDBApi.getSession().getAccessTokenPair();
                } catch (IllegalStateException e) {
                    Log.i("DbAuthLog", "Error authenticating", e);
                }
            }
        }

        super.onResume();
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
            try {
                boolean logged = mDBApi.getSession().isLinked();
                DropboxAPI.Account account = mDBApi.accountInfo();
                DropboxAPI.Entry albumEntry = mDBApi.createFolder(path[0]);

            } catch (DropboxException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String response) {

        }
    }


}
