package pt.ulisboa.tecnico.cmov.p2photo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SavePhotoService extends Service {

    private GlobalClass global;
    String album, photoName, loginToken, user;
    Bitmap photo;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        global = (GlobalClass) getApplicationContext();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        // Toast.makeText(this, "Saving photo...", Toast.LENGTH_SHORT).show();
        album = intent.getStringExtra("album");
        photoName = intent.getStringExtra("photoName");
        photo = global.getServicePhoto();

        new UploadPhotoTask().execute(album);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){

    }

    @SuppressLint("NewApi")
    class UploadPhotoTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... album){
            Bitmap photoToKeep = Bitmap.createBitmap(photo);

            try{
                // Create new photo in folder
                String photoPath = getApplicationContext().getFilesDir() + "/" + album[0] + "/" + photoName;
                File file = new File(photoPath);
                if(!file.exists()){
                    file.createNewFile();
                }

                try(FileOutputStream out = new FileOutputStream(photoPath)){
                    photo.compress(Bitmap.CompressFormat.PNG, 0, out);

                } catch(IOException e){
                    e.printStackTrace();
                }

                // Update album catalog
                String catalogPath = getApplicationContext().getFilesDir() + "/" + album[0] + "/index.txt";
                try(FileOutputStream out = new FileOutputStream(catalogPath,true)) {
                    String writePath = photoPath + "\n";
                    byte[] data = writePath.getBytes();
                    out.write(data);

                } catch(IOException e){
                    e.printStackTrace();
                }

                if(!global.containsPhoto(album[0], photoPath)){
                    global.addPhotoToAlbum(album[0], photoToKeep, photoPath);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return photoName;
        }

        @Override
        protected void onPostExecute(String string){
            if(string == null){
                Toast toast = Toast.makeText(getApplicationContext(), "Could not save", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Saved photo " + string + " to album " + album, Toast.LENGTH_SHORT);
                toast.show();
            }
        }

    }
}
