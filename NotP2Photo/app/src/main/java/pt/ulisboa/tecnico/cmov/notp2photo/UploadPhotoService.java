package pt.ulisboa.tecnico.cmov.notp2photo;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UploadPhotoService extends Service {

    private GlobalClass global;
    DbxClientV2 client;
    String accessToken;
    String album, photoName, loginToken, user;
    Bitmap photo;

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

                String previousCatalog = baos.toString();
                String newCatalog = "";
                String imageURL = photoLink.getUrl().replace("dl=0", "raw=1");

                if (previousCatalog.isEmpty())
                    newCatalog = imageURL;
                else
                    newCatalog = previousCatalog + "\n" + imageURL;

                InputStream targetStream = new ByteArrayInputStream(newCatalog.getBytes());
                client.files().delete(catalogPath);
                client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);

                SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                String url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + loginToken + "&album=" + album[0];
                WebInterface.post(url, linkMetadata.getUrl());

                photoName = metadata.getName();

            } catch (UploadErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            global.addPhotoToAlbum(album[0], photo);
            return photoName;
        }

        @Override
        protected void onPostExecute(String string){
            if(string == null){
                Toast toast = Toast.makeText(getApplicationContext(), "Upload not okay", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Uploaded: " + string, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
}
