package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;

public class ViewAlbumActivity extends AppCompatActivity {
    private Context context = this;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);

        global = (GlobalClass) getApplicationContext();

        Intent intent = getIntent();
        String album = intent.getStringExtra("album");

        if(!DownloadPhotosService.containsAlbum(album)){
            DownloadPhotosService.addAlbum(album);
            intent = new Intent(this, DownloadPhotosService.class);
            intent.putExtra("album", album);
            startService(intent);
        }

        ArrayList<Bitmap> photoBitMap = global.getAlbumPhotoList(album);
        Bitmap[] result = new Bitmap[photoBitMap.size()];
        result = photoBitMap.toArray(result);
        setGridView(result);
    }

    public void setGridView(final Bitmap[] bm){
        GridView gridView = (GridView) findViewById(R.id.gridAlbum);
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
