package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.net.URL;

public class ViewPhotoActivity extends AppCompatActivity {
    private String link;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        Intent intent = getIntent();
        link = intent.getStringExtra("Link");

        new ImageDownloader().execute();
    }

    private class ImageDownloader extends AsyncTask<Void, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URL url = new URL(link);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bm) {
            imageView = (ImageView) findViewById(R.id.photo);
            imageView.setImageBitmap(bm);
        }
    }
}
