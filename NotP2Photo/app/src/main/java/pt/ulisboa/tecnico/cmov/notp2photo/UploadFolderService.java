package pt.ulisboa.tecnico.cmov.notp2photo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadFolderService extends Service {

    private String accessToken, token, user;
    private DbxClientV2 client;
    private GlobalClass global;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        global = (GlobalClass) getApplicationContext();

        accessToken = global.getUserAccessToken();
        token = global.getUserLoginToken();
        user = global.getUserName();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "Creating Album...", Toast.LENGTH_SHORT).show();
        String album = intent.getStringExtra("album");
        global.addNewAlbum(album);

        new CreateAlbumTask().execute(album);
        return START_STICKY;
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
                String url = "http://" + WebInterface.IP + "/createAlbum?name="+user+"&token="+token+"&album="+path[0];
                Log.d(MainActivity.TAG, "URL: " + url);
                WebInterface.get(url);

                // Create new blank file in the created folder and put its link in the server
                String catalogPath = "/P2Photo/" + path[0] + "/index.txt";
                InputStream targetStream = new ByteArrayInputStream("".getBytes());
                client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);

                SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + token + "&album=" + path[0];
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
                Toast toast = Toast.makeText(getApplicationContext(), "New album created!", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Could not create album", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
