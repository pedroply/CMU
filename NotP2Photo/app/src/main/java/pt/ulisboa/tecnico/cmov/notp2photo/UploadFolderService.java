package pt.ulisboa.tecnico.cmov.notp2photo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class UploadFolderService extends Service {

    private String accessToken, token, user;
    private DbxClientV2 client;
    private GlobalClass global;
    private KeyPair keyPair;

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
        keyPair = global.getUserKeyPair();
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

                //generateRandom AES key to encript the index file (needs also iv)
                String encriptedKeyBase64 = "";
                String ivBase64 = "";
                Key k = null;
                byte[] indexBytes = {};
                try {
                    k = SymmetricCrypto.generateRandomKey();
                    byte[] encriptedKey = RSAGenerator.encrypt(keyPair.getPublic(), k.getEncoded());
                    encriptedKeyBase64 = Base64.encodeToString(encriptedKey, Base64.DEFAULT);
                    ivBase64 = Base64.encodeToString(SymmetricCrypto.generateNewIv().getIV(), Base64.DEFAULT);
                    indexBytes = SymmetricCrypto.encrypt(k,"emptyemptyemptyemptyemptyemptyemptyemptyemptyempty".getBytes(), Base64.decode(ivBase64, Base64.DEFAULT));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // TODO: DO SOMETHING WITH RESPONSES
                String url = "http://" + WebInterface.IP + "/createAlbum?name="+user+"&token="+token+"&album="+path[0];
                Log.d(MainActivity.TAG, "URL: " + url);
                WebInterface.post(url, encriptedKeyBase64);

                // Create new blank file in the created folder and put its link in the server
                String catalogPath = "/P2Photo/" + path[0] + "/index.txt";
                InputStream targetStream = new ByteArrayInputStream(indexBytes);
                client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);

                SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + token + "&album=" + path[0];
                WebInterface.post(url, linkMetadata.getUrl()+"\n"+ivBase64);

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
