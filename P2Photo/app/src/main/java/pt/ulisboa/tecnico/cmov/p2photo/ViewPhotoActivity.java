package pt.ulisboa.tecnico.cmov.p2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ViewPhotoActivity extends AppCompatActivity {
    private ImageView imageView;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        global = (GlobalClass) getApplicationContext();

        imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(global.getSelectedPhoto());

    }
}