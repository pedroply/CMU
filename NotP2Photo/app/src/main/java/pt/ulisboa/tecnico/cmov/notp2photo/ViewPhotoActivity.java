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
    private Bitmap bitmapLink;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra("link");
        bitmapLink = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(bitmapLink);

    }
}
