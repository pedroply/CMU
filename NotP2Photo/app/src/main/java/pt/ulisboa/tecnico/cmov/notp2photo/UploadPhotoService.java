package pt.ulisboa.tecnico.cmov.notp2photo;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;

public class UploadPhotoService extends Service {

    private GlobalClass global;
    DbxClientV2 client;
    String accessToken;
    String album, photoName, loginToken, user;
    Bitmap photo;
    private KeyPair keyPair;
    private String TAG = "uploadPhotoService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        global = (GlobalClass) getApplicationContext();
        accessToken = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();
        keyPair = global.getUserKeyPair();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show();
        album = intent.getStringExtra("album");
        photoName = intent.getStringExtra("photoName");
        photo = global.getServicePhoto();

        new UploadPhotoTask().execute(album);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){

    }

    class UploadPhotoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... album){
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            Log.i(MainActivity.TAG, accessToken);
            client = new DbxClientV2(config, accessToken);

            Bitmap photoToKeep = Bitmap.createBitmap(photo);

            try{
                // Compress photo to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

                // Upload photo and create shared link
                String photoPath = "/P2Photo/" + album[0] + "/" + photoName;
                FileMetadata metadata = client.files().uploadBuilder(photoPath).uploadAndFinish(bs);
                SharedLinkMetadata photoLink = client.sharing().createSharedLinkWithSettings(photoPath);

                // Update album catalog
                String catalogPath = "/P2Photo/" + album[0] + "/index.txt";

                DbxDownloader<FileMetadata> download = client.files().download(catalogPath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                download.download(baos);

                //getting the key to decript the old index and encript new onde
                String url = "http://" + WebInterface.IP + "/retriveAlbum?name=" + user + "&token=" + loginToken + "&album=" + album[0];
                String response = WebInterface.get(url);
                JSONObject mainObject = new JSONObject(response);

                //get encriptedSymetrickey and decripte it with my privkey
                String encriptedKeyBase64 = mainObject.getJSONObject("encriptedKeys").getString(user);
                byte[] encodedKey = RSAGenerator.decrypt(keyPair.getPrivate(), Base64.decode(encriptedKeyBase64, Base64.DEFAULT));
                Key k = SymmetricCrypto.generateKeyFromEncodedKey(encodedKey);

                //get iv
                JSONArray linkArray = mainObject.getJSONArray("linksIvs");
                String ivBase64 = null;
                for(int i = 0; i < linkArray.length(); i++) {
                    if(linkArray.getJSONArray(i).getString(2).equals(user)) {
                        ivBase64 = linkArray.getJSONArray(i).getString(1);
                        break;
                    }
                }
                if(ivBase64 == null){
                    //no iv found, this should never happen (for one user)
                    throw new Exception("No ivfound!");
                }

                //decript baos first
                byte[] encriptedIndexFile = baos.toByteArray();
                String decriptedIndexString = new String(SymmetricCrypto.decrypt(k, encriptedIndexFile, Base64.decode(ivBase64, Base64.DEFAULT)));


                String previousCatalog = decriptedIndexString;
                String newCatalog = "";
                String imageURL = photoLink.getUrl().replace("dl=0", "raw=1");

                if(!global.containsPhoto(album[0], imageURL)){
                    global.addPhotoToAlbum(album[0], photoToKeep, imageURL);
                }

                Log.i(TAG, "previous catalog: " + previousCatalog);

                if (previousCatalog.equals("emptyemptyemptyemptyemptyemptyemptyemptyemptyempty"))
                    newCatalog = imageURL;
                else
                    newCatalog = previousCatalog + "\n" + imageURL;

                Log.i(TAG, "new catalog: " + newCatalog);

                //needs to encript before sending
                ivBase64 = Base64.encodeToString(SymmetricCrypto.generateNewIv().getIV(), Base64.DEFAULT);
                byte[] indexBytes = SymmetricCrypto.encrypt(k,newCatalog.getBytes(), Base64.decode(ivBase64, Base64.DEFAULT));

                InputStream targetStream = new ByteArrayInputStream(indexBytes);
                client.files().delete(catalogPath);
                client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);

                //udate link and iv
                SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + loginToken + "&album=" + album[0];
                WebInterface.post(url, linkMetadata.getUrl()+"\n"+ivBase64);

                photoName = metadata.getName();

            } catch (UploadErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return photoName;
        }

        @Override
        protected void onPostExecute(String string){
            if(string == null){
                Toast toast = Toast.makeText(getApplicationContext(), "Upload not okay", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Uploaded " + string + " to album " + album, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
}
