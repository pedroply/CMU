package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class CreateAlbum extends AppCompatActivity {

    private String token, user;
    private Context context = this;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);

        global = (GlobalClass) getApplicationContext();
        token = global.getUserLoginToken();
        user = global.getUserName();

    }

    public void createAlbum(View v){
        EditText albumNameText = findViewById(R.id.editAlbumName);
        String albumName = albumNameText.getText().toString();

        // Toast.makeText(this, "Creating Album...", Toast.LENGTH_SHORT).show();
        new CreateAlbumTask().execute(albumName);
    }

    class CreateAlbumTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... path) {

            try {
                String url = "http://" + WebInterface.IP + "/createAlbum?name="+user+"&token="+token+"&album="+path[0];
                String response = WebInterface.get(url);
                if(response == null)
                    return null;

                global.addNewAlbum(path[0]);

                // Create folder locally
                File album = new File( context.getFilesDir() + "/" + path[0]);
                album.mkdir();

                File indexFile = new File(context.getFilesDir() + "/" + path[0] + "/" + "index.txt");
                indexFile.createNewFile();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return path[0];
        }

        @Override
        protected void onPostExecute(String response) {
            if(response != null){
                Toast toast = Toast.makeText(context, "New album created!", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(context, "Could not create album", Toast.LENGTH_SHORT);
                toast.show();
            }
            finish();
        }
    }


}