package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

import java.util.ArrayList;
import java.util.List;

public class ChooseAlbumUserActivity extends AppCompatActivity {
    private Context context = this;
    private DbxClientV2 client;
    private String accessToken, loginToken, user;
    private String userName;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_album_user);

        global = (GlobalClass) getApplicationContext();
        Intent intent = getIntent();

        accessToken = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();
        userName = intent.getStringExtra("user");

        //TODO: Get albuns not shared with the other user
        setListView(global.getAlbumList());
    }

    private void setListView(ArrayList<String> list){
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_home_album_view, list);

        final ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(adapter);

        // TODO: Make this async
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String album = (String) listView.getItemAtPosition(position);
                new shareAlbumWithUsersTask().execute(album);

                Toast.makeText(getApplicationContext(), "Added user " + userName + " to album " + album, Toast.LENGTH_SHORT).show();

                finish();
            }
        });
    }

    private class shareAlbumWithUsersTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... album){
            String url = "http://" + WebInterface.IP + "/addClient2Album?name=" + user + "&token=" + loginToken + "&album=" + album[0] + "&client2Add=" + userName;
            WebInterface.get(url);

            return null;
        }

    }
}
