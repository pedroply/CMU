package pt.ulisboa.tecnico.cmov.notp2photo;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class DownloadPhotosService extends Service {

    private GlobalClass global;
    private DbxClientV2 client;
    private String token, loginToken, user, album;
    private ArrayList<String> bitmaps = new ArrayList<String>();
    private static ArrayList<String> downloadingAlbums = new ArrayList<String>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        global = (GlobalClass) getApplicationContext();
        token = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        album = intent.getStringExtra("album");
        Toast toast = Toast.makeText(getApplicationContext(), "Getting photos from album " + album + "...", Toast.LENGTH_SHORT);
        toast.show();

        new ImageDownloader().execute();
        stopSelf(startId);
        return START_STICKY;
    }

    private class ImageDownloader extends AsyncTask<Void, Void, Bitmap[]> {

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            ArrayList<Bitmap> photoBitMap = new ArrayList<Bitmap>();
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            client = new DbxClientV2(config, token);

            String url = "http://" + WebInterface.IP + "/retriveAlbum?name=" + user + "&token=" + loginToken + "&album=" + album;
            String response = WebInterface.get(url);
            Log.i(MainActivity.TAG, "ViewResponse: " + response);

            try {
                JSONObject mainObject = new JSONObject(response);
                JSONArray linkArray = mainObject.getJSONArray("links");

                for(int i = 0; i < linkArray.length(); i++) {
                    // Get the catalog file links
                    String catalogLink = linkArray.getString(i);
                    Log.i(MainActivity.TAG, "LINK: " + catalogLink);

                    DbxDownloader<SharedLinkMetadata> downloader = client.sharing().getSharedLinkFile(catalogLink);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    downloader.download(baos);
                    Log.i(MainActivity.TAG, "BAOS: " + baos.toString());

                    if (baos.toString().isEmpty())
                        continue;
                    String[] photoLinks = baos.toString().split("\n");

                    // Get the bitmaps of each photo
                    for (String link : photoLinks) {
                        bitmaps.add(link);
                        URL photoURL = new URL(link);

                        Bitmap bitmap = BitmapFactory.decodeStream(photoURL.openStream());
                        global.addPhotoToAlbum(album, bitmap, link);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            photoBitMap = global.getAlbumPhotoList(album);
            Bitmap[] result = new Bitmap[photoBitMap.size()];
            result = photoBitMap.toArray(result);
            return result;
        }

        @Override
        protected void onPostExecute(final Bitmap[] bm) {
            if(bm != null){
                Toast toast = Toast.makeText(getApplicationContext(), "Downloaded photos for album " + album, Toast.LENGTH_SHORT);
                toast.show();
            }
            
        }

    }

    public static synchronized void addAlbum(String album){
        downloadingAlbums.add(album);
    }

    public static synchronized boolean containsAlbum(String album){
        return downloadingAlbums.contains(album);
    }

    public static synchronized void clearAlbums(){
        downloadingAlbums.clear();
    }
}
