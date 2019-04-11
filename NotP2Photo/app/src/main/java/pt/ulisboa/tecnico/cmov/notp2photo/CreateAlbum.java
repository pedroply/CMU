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
import java.util.ArrayList;

public class CreateAlbum extends AppCompatActivity {

    DbxClientV2 client;
    String accessToken = "", token;
    String user;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        global = (GlobalClass) getApplicationContext();
        accessToken = global.getUserAccessToken();
        token = global.getUserLoginToken();
        user = global.getUserName();

    }

    public void createAlbum(View v){
        EditText albumNameText = findViewById(R.id.editAlbumName);
        String albumName = albumNameText.getText().toString();

        Intent intent = new Intent(this, UploadFolderService.class);
        intent.putExtra("album", albumName);
        startService(intent);

        finish();
    }


}
