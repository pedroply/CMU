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

        UploadPhotoTask task = new UploadPhotoTask();
        task.onPreExecute();
        task.execute(album);
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


    class UploadPhotoTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute(){
            TextView text = (TextView) findViewById(R.id.uploadText);
            text.setText("Uploading photo...");
        }

        @Override
        protected String doInBackground(String... album){
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            Log.i(MainActivity.TAG, accessToken);
            client = new DbxClientV2(config, accessToken);

            try{
                // Compress photo to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapdata = bos.toByteArray();
                ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

                // Upload photo and create shared link
                String photoPath = "/P2Photo/" + album[0] + "/" + photoName;
                FileMetadata metadata = client.files().uploadBuilder(photoPath).uploadAndFinish(bs);
                SharedLinkMetadata photoLink = client.sharing().createSharedLinkWithSettings(photoPath);

                // Update album catalog
                String catalogPath = "/P2Photo/" + album[0] + "/index.txt";
                DbxDownloader<FileMetadata> download = client.files().download(catalogPath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                download.download(baos);

                String previousCatalog = baos.toString();
                String newCatalog = "";
                String imageURL = photoLink.getUrl().replace("dl=0", "raw=1");

                if (previousCatalog.isEmpty())
                    newCatalog = imageURL;
                else
                    newCatalog = previousCatalog + "\n" + imageURL;

                InputStream targetStream = new ByteArrayInputStream(newCatalog.getBytes());
                client.files().delete(catalogPath);
                client.files().uploadBuilder(catalogPath).uploadAndFinish(targetStream);

                SharedLinkMetadata linkMetadata = client.sharing().createSharedLinkWithSettings(catalogPath);
                String url = "http://" + WebInterface.IP + "/postLink?name=" + user + "&token=" + loginToken + "&album=" + album[0];
                WebInterface.post(url, linkMetadata.getUrl());

                photoName = metadata.getName();

            } catch (UploadErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            global.addPhotoToAlbum(album[0], photo);
            return photoName;
        }

        @Override
        protected void onPostExecute(String string){
            if(string == null){
                Toast toast = Toast.makeText(getApplicationContext(), "Upload not okay", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Uploaded: " + string, Toast.LENGTH_SHORT);
                toast.show();
            }

            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
        }

    }
}
