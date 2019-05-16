package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChooseAlbumUserActivity extends AppCompatActivity {
    private Context context = this;
    private String loginToken, user;
    private String userName;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_album_user);

        global = (GlobalClass) getApplicationContext();
        Intent intent = getIntent();

        loginToken = global.getUserLoginToken();
        user = global.getUserName();
        userName = intent.getStringExtra("user");

        new getUserAlbums().execute();

    }

    private void setListView(ArrayList<String> list){
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_home_album_view, list);

        final ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(adapter);

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

    private class getUserAlbums extends AsyncTask<Void, Void , ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... rndm){

            ArrayList<String> albums = global.getAlbumList();
            ArrayList<String> albumsToShare = new ArrayList<String>();

            try {

                for (String album : albums) {
                    String url = "http://" + WebInterface.IP + "/retriveAlbum?name=" + user + "&token=" + loginToken + "&album=" + album;
                    String response = WebInterface.get(url);
                    if(response == null)
                        return null;

                    JSONObject mainObject = new JSONObject(response);
                    JSONArray linkArray = mainObject.getJSONArray("clients");

                    if(!alreadyShared(linkArray)){
                        albumsToShare.add(album);
                    }

                    global.addNewAlbumShared(album);
                    global.addUsersSharedWithAlbum(album, linkArray);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return albumsToShare;
        }

        @Override
        protected void onPostExecute(ArrayList<String> albums){
            if(albums == null) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                return;
            }
            if(albums.isEmpty()){
                Toast.makeText(context, "No albums left to share", Toast.LENGTH_SHORT).show();
            }
            setListView(albums);
        }

    }

    private class shareAlbumWithUsersTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... album){
            String url = "http://" + WebInterface.IP + "/addClient2Album?name=" + user + "&token=" + loginToken + "&album=" + album[0] + "&client2Add=" + userName;
            WebInterface.get(url);

            return null;
        }

    }

    private boolean alreadyShared(JSONArray linkArray){
        try{
            for (int i = 0; i < linkArray.length(); i++) {
                String client = linkArray.getString(i);
                if (client.equals(userName)) {
                    return true;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
