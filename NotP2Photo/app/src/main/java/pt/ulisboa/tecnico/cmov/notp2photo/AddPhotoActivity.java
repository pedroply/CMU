package pt.ulisboa.tecnico.cmov.notp2photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class AddPhotoActivity extends AppCompatActivity {

    int REQUEST_GET_SINGLE_FILE;
    DbxClientV2 client;
    String accessToken = "";
    String album, photoName, loginToken, user;
    Bitmap photo;
    private Context context = this;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_photo);

        global = (GlobalClass) getApplicationContext();
        Intent intent = getIntent();
        accessToken = global.getUserAccessToken();
        album = intent.getStringExtra("album");
        loginToken = global.getUserLoginToken();
        user = global.getUserName();
    }

    public void uploadPhoto(View v){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_GET_SINGLE_FILE);
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
            }

        } catch(Exception e){
            e.printStackTrace();
            return;
        }

        global.setServicePhoto(photo);
        Intent intent = new Intent(this, UploadPhotoService.class);
        intent.putExtra("album", album);
        intent.putExtra("photoName", photoName);
        startService(intent);

        finish();
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
