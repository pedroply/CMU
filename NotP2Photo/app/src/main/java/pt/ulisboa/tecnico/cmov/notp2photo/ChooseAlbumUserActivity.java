package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
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
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChooseAlbumUserActivity extends AppCompatActivity {
    private Context context = this;
    private DbxClientV2 client;
    private String accessToken, loginToken, user;
    private String userName;
    private GlobalClass global;
    private String pubKeyBase64;
    private HashMap<String, String> albumKeys = new HashMap<>();
    private KeyPair keyPair;
    private static String TAG = "ChooseAlbumUserActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_album_user);

        global = (GlobalClass) getApplicationContext();
        Intent intent = getIntent();

        accessToken = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();
        keyPair = global.getUserKeyPair();

        userName = intent.getStringExtra("user");
        pubKeyBase64 = intent.getStringExtra("pubKeyBase64");

        new getUserAlbums().execute();

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

    private class getUserAlbums extends AsyncTask<Void, Void , ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(Void... rndm){

            ArrayList<String> albums = global.getAlbumList();
            ArrayList<String> albumsToShare = new ArrayList<String>();

            try {

                for (String album : albums) {
                    String url = "http://" + WebInterface.IP + "/retriveAlbum?name=" + user + "&token=" + loginToken + "&album=" + album;
                    String response = WebInterface.get(url);

                    JSONObject mainObject = new JSONObject(response);
                    JSONArray linkArray = mainObject.getJSONArray("clients");

                    if(!alreadyShared(linkArray)){
                        albumsToShare.add(album);
                        //get encripted album key with my pub key
                        albumKeys.put(album, mainObject.getJSONObject("encriptedKeys").getString(user));
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return albumsToShare;
        }

        @Override
        protected void onPostExecute(ArrayList<String> albums){
            if(albums.isEmpty()){
                Toast.makeText(context, "No albums left to share", Toast.LENGTH_SHORT).show();
            }
            setListView(albums);
        }

    }

    private class shareAlbumWithUsersTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... album){
            //retrive allbum key and encripted with client to add pubkey
            String url = "http://" + WebInterface.IP + "/addClient2Album?name=" + user + "&token=" + loginToken + "&album=" + album[0] + "&client2Add=" + userName;
            //decript to get original
            byte[] encriptedWithClient2AddPubKey = null;

            try {
                PrivateKey p = keyPair.getPrivate();
                byte[] encodedkeyEncripted = Base64.decode(albumKeys.get(album[0]), Base64.DEFAULT);
                byte[] encodedKey = RSAGenerator.decrypt(p, encodedkeyEncripted);
                PublicKey clietn2AddPubKey = RSAGenerator.readPublicKeyBase64(pubKeyBase64);
                encriptedWithClient2AddPubKey = RSAGenerator.encrypt(clietn2AddPubKey, encodedKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            WebInterface.post(url, Base64.encodeToString(encriptedWithClient2AddPubKey, Base64.DEFAULT));

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
