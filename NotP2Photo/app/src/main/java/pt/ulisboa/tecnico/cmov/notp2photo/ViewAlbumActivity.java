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
    private GlobalClass global;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        global = (GlobalClass) getApplicationContext();

        Intent intent = getIntent();
        album = intent.getStringExtra("album");
        token = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();

        bitmaps = new ArrayList<>();

        if(global.albumPhotosIsEmpty(album) && !DownloadPhotosService.downloadingAlbums.contains(album)){
            DownloadPhotosService.downloadingAlbums.add(album);
            intent = new Intent(this, DownloadPhotosService.class);
            intent.putExtra("album", album);
            startService(intent);
        }

        ArrayList<Bitmap> photoBitMap = global.getAlbumPhotoList(album);
        Bitmap[] result = new Bitmap[photoBitMap.size()];
        result = photoBitMap.toArray(result);
        setGridView(result);
    }

    private void setGridView(final Bitmap[] bm){
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
}
