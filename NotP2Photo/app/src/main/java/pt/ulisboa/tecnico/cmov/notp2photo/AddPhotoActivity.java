package pt.ulisboa.tecnico.cmov.notp2photo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.InputStream;

public class AddPhotoActivity extends AppCompatActivity {

    int REQUEST_GET_SINGLE_FILE;
    private String album, photoName;
    private Bitmap photo;
    private Button uploadButton;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        global = (GlobalClass) getApplicationContext();
        Intent intent = getIntent();
        album = intent.getStringExtra("album");

        uploadButton = (Button) findViewById(R.id.buttonUpload);
        uploadButton.setEnabled(false);
        uploadButton.setAlpha((float) 0.5);
    }

    public void selectPhoto(View v){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
    }

    public void uploadPhoto(View v) {
        global.setServicePhoto(photo);
        Intent intent = new Intent(this, UploadPhotoService.class);
        intent.putExtra("album", album);
        intent.putExtra("photoName", photoName);
        startService(intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(resultCode == RESULT_OK){
                final Uri imageUri = data.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                photo = BitmapFactory.decodeStream(imageStream);

                dumpImageMetaData(imageUri);
                ImageView photoView = (ImageView) findViewById(R.id.photoView);
                photoView.setImageBitmap(photo);
                uploadButton.setEnabled(true);
                uploadButton.setAlpha((float) 1);
            }

        } catch(Exception e){
            e.printStackTrace();
            return;
        }
    }

    @SuppressLint("NewApi")
    public void dumpImageMetaData(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);

        try {
            if (cursor != null && cursor.moveToFirst()) {
                photoName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(MainActivity.TAG, "Display Name: " + photoName);
            }
        } finally {
            cursor.close();
        }
    }
}
