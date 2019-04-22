package pt.ulisboa.tecnico.cmov.p2photo;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ViewAlbumActivity extends AppCompatActivity {
    private Context context = this;
    private String album, loginToken, user;
    private List<String> bitmaps;
    private GridView gridView;
    private GlobalClass global;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        global = (GlobalClass) getApplicationContext();

        Intent intent = getIntent();
        album = intent.getStringExtra("album");
        loginToken = global.getUserLoginToken();
        user = global.getUserName();

        bitmaps = new ArrayList<>();

        if(!global.isDownloaded(album)){
            global.addDownload(album);

            Toast toast = Toast.makeText(getApplicationContext(), "Getting photos from album " + album + "...", Toast.LENGTH_SHORT);
            toast.show();

            new ImageDownloader().execute();
        }

        ArrayList<Bitmap> photoBitMap = global.getAlbumPhotoList(album);
        Bitmap[] result = new Bitmap[photoBitMap.size()];
        result = photoBitMap.toArray(result);
        setGridView(result);
    }

    public void setGridView(final Bitmap[] bm){
        gridView = (GridView) findViewById(R.id.gridAlbum);
        ImageAdapter adapter = new ImageAdapter(context,bm);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bitmap bitmap = bm[position];
                global.setSelectedPhoto(bitmap);

                Intent intent = new Intent(context, ViewPhotoActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("NewApi")
    private class ImageDownloader extends AsyncTask<Void, Void, Bitmap[]> {

        @Override
        protected Bitmap[] doInBackground(Void... voids) {
            ArrayList<Bitmap> photoBitMap = new ArrayList<Bitmap>();

            //TODO: Several user catalogs

            String catalogPath = context.getFilesDir() + "/" + album + "/index.txt";
            try(FileInputStream fis = new FileInputStream(catalogPath)){
                Scanner scanner = new Scanner(fis);

                while(scanner.hasNextLine()){
                    String path = scanner.nextLine();

                    if(!global.containsPhoto(album, path)){
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

                        global.addPhotoToAlbum(album, bitmap, path);
                    }
                }

            } catch(IOException e) {
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
                setGridView(bm);
            }

        }

    }
}
