package pt.ulisboa.tecnico.cmov.notp2photo;

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

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ChooseAlbumActivity extends AppCompatActivity {

    DbxClientV2 client;
    String accessToken, loginToken, user;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_album);

        global = (GlobalClass) getApplicationContext();
        accessToken = global.getUserAccessToken();

        setListView(global.getAlbumList());
    }

    /*private class AlbumLoaderTask extends AsyncTask<Void, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... voids) {
            ArrayList<String> albumList = new ArrayList<>();
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            client = new DbxClientV2(config, accessToken);

            try {
                List<Metadata> folders = client.files().listFolder("/P2Photo").getEntries();
                for (Metadata md : folders) {
                    albumList.add(md.getName());
                }
            } catch (DbxException e) {
                e.printStackTrace();
            }

            return albumList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> list) {

        }
    }*/

    private void setListView(ArrayList<String> list){
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_home_album_view, list);

        final ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String album = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "You selected : " + album, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), AddPhotoActivity.class);
                intent.putExtra("album", album);
                startActivity(intent);

                finish();
            }
        });
    }
}
