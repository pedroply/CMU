package pt.ulisboa.tecnico.cmov.notp2photo;

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

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxRawClientV2;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ViewAlbumActivity extends AppCompatActivity {
    private Context context = this;
    private List<String> links;
    private String album, token, loginToken;
    private DbxClientV2 client;
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        Intent intent = getIntent();
        album = intent.getStringExtra("album");
        loginToken = intent.getStringExtra("loginToken");
        token = intent.getStringExtra("token");
        links = new ArrayList<>();
        links.add("https://cdn.pixabay.com/photo/2016/04/15/04/02/water-1330252__340.jpg");
        links.add("https://www.topimagens.com.br/imagens/imagens-mais-novas.jpg");
        links.add("https://i.pinimg.com/736x/92/09/5e/92095ed056a02f384111be6c40fb28e0.jpg");
        links.add("https://cdn.pixabay.com/photo/2017/09/05/23/01/background-2719572_960_720.jpg");

        new ImageDownloader().execute();
    }

    private class ImageDownloader extends AsyncTask<Void, Void, Bitmap[]> {

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
    }
}
