package pt.ulisboa.tecnico.cmov.notp2photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxRawClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.GetSharedLinkFileErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.users.FullAccount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ViewAlbumActivity extends AppCompatActivity {
    private Context context = this;
    private String album, token, loginToken, user;
    private List<String> bitmaps;
    private DbxClientV2 client;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        Intent intent = getIntent();
        album = intent.getStringExtra("album");
        token = intent.getStringExtra("token");
        loginToken = intent.getStringExtra("loginToken");
        user = intent.getStringExtra("user");

        bitmaps = new ArrayList<>();
        new ImageDownloader().execute();
    }

    private class ImageDownloader extends AsyncTask<Void, Void, Bitmap[]> {

        @Override
        public void onPreExecute(){
            Toast toast = Toast.makeText(getApplicationContext(), "Getting photos from album...", Toast.LENGTH_SHORT);
            toast.show();
        }

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
                        break;
                    String[] photoLinks = baos.toString().split("\n");

                    // Get the bitmaps of each photo
                    for (String link : photoLinks) {
                        bitmaps.add(link);
                        URL photoURL = new URL(link);

                        Bitmap bitmap = BitmapFactory.decodeStream(photoURL.openStream());
                        photoBitMap.add(bitmap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap[] result = new Bitmap[photoBitMap.size()];
            result = photoBitMap.toArray(result);
            return result;
        }

        // DOES NOT WORK
        @Override
        protected void onPostExecute(final Bitmap[] bm) {
            gridView = (GridView) findViewById(R.id.gridAlbum);
            ImageAdapter adapter = new ImageAdapter(context,bm);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bitmap bitmap = bm[position];

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    byte[] byteArray = stream.toByteArray();

                    byte[] byteArray1 = new byte[(byteArray.length + 1) / 2];
                    byte[] byteArray2 = new byte[byteArray.length - byteArray1.length];

                    Intent intent = new Intent(context, ViewPhotoActivity.class);
                    int size = byteArray.length;
                    intent.putExtra("link", byteArray1);
                    intent.putExtra("link1", byteArray2);
                    startActivity(intent);
                }
            });
        }

    }
}
