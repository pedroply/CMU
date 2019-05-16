package pt.ulisboa.tecnico.cmov.notp2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class ViewPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        GlobalClass global = (GlobalClass) getApplicationContext();

        ImageView imageView = (ImageView) findViewById(R.id.photo);
        imageView.setImageBitmap(global.getSelectedPhoto());

    }
}
