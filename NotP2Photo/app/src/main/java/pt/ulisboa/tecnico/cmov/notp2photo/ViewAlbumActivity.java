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
import com.dropbox.core.v2.users.FullAccount;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ViewAlbumActivity extends AppCompatActivity {
    public static byte[] chosenPhotoBytes;
    private Context context = this;
    private String album, token;
    private List<byte[]> bitmaps;
    private DbxClientV2 client;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        Intent intent = getIntent();
        album = intent.getStringExtra("album");
        token = intent.getStringExtra("token");

        bitmaps = new ArrayList<>();
        new ImageDownloader().execute();
    }

    private class ImageDownloader extends AsyncTask<Void, Void, Bitmap[]> {

        @Override
        public void onPreExecute(){
            Toast toast = Toast.makeText(getApplicationContext(), "Getting photos from album...", Toast.LENGTH_SHORT);
            toast.show();
        }

        @SuppressLint("NewApi")
        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            ArrayList<Bitmap> photoBitMap = new ArrayList<Bitmap>();

            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            Log.i(MainActivity.TAG, token);
            client = new DbxClientV2(config, token);
            // TODO: Get server links, access the link file, download all the images in those links
            try{
                ListFolderResult listFolder = client.files().listFolder("/P2Photo/" + album);
                List<Metadata> photosMetadata = listFolder.getEntries();

                for(Metadata metadata : photosMetadata){
                    DbxDownloader<FileMetadata> download = client.files().download("/P2Photo/" + album + "/" + metadata.getName());

                    try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
                        download.download(baos);

                        byte[] bitmapdata = baos.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

                        bitmaps.add(bitmapdata);
                        photoBitMap.add(bitmap);
                    }

                }


            } catch (ListFolderErrorException e) {
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

        @Override
        protected void onPostExecute(Bitmap[] bm) {
            gridView = (GridView) findViewById(R.id.gridAlbum);
            gridView.setAdapter(new ImageAdapter(context, bm));

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    chosenPhotoBytes = bitmaps.get(position);
                    Intent intent = new Intent(context, ViewPhotoActivity.class);
                    // byte[] too large for intent
                    // intent.putExtra("ByteArray", bitmaps.get(position));
                    startActivity(intent);
                }
            });
        }

    }


    /*private class ImageDownloader extends AsyncTask<Void, Void, Bitmap[]> {

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            try {
                // Só a testar aqui umas imagens, don't mind me
                URL end1 = new URL(links.get(0));
                Bitmap image1 = BitmapFactory.decodeStream(end1.openStream());
                URL end2 = new URL(links.get(1));
                Bitmap image2 = BitmapFactory.decodeStream(end2.openStream());
                URL end3 = new URL(links.get(2));
                Bitmap image3 = BitmapFactory.decodeStream(end3.openStream());
                URL end4 = new URL(links.get(3));
                Bitmap image4 = BitmapFactory.decodeStream(end4.openStream());
                Bitmap[] cenas = {image1, image2, image3, image4};
                return cenas;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap[] bm) {
            gridView = (GridView) findViewById(R.id.gridAlbum);
            gridView.setAdapter(new ImageAdapter(context, bm));

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(context, ViewPhotoActivity.class);
                    intent.putExtra("Link", links.get(position));
                    startActivity(intent);
                }
            });
        }
    }*/
}
