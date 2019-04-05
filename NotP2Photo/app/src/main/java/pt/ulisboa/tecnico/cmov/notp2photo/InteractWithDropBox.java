package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.users.FullAccount;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InteractWithDropBox extends AppCompatActivity {

    private static final String ACCESS_TOKEN = "A4Fx_jmriwQAAAAAAAAA9EzYY69YksldOyfNzc93zqs6vxjlV0H2crW4M-ViEelV";

    DbxClientV2 client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_interact_with_drop_box);


    }

    public void login(View v){
        // MyActivity below should be your activity class name
    }

    public void uploadData(View v){
        //EditText dataTextInput = findViewById(R.id.dataInputText);
        //String data = dataTextInput.getText().toString();
        Log.i("DbExampleLog", "Starting upload");
        new uploadTask().execute("");
    }

    public void uploadData(String data) throws IOException, DbxException {
        Log.i("DbExampleLog", "TO upload: " + data);
        InputStream targetStream = new ByteArrayInputStream(data.getBytes());

        // Upload "test.txt" to Dropbox
        FileMetadata metadata = client.files().uploadBuilder("/test.txt").uploadAndFinish(targetStream);
    }

    class uploadTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... data) {
            Log.i("DbExampleLog", "Background");
            // Create Dropbox client
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
            client = new DbxClientV2(config, ACCESS_TOKEN);

            // Get current account info
            FullAccount account = null;
            try {
                account = client.users().getCurrentAccount();
            } catch (DbxException e) {
                e.printStackTrace();
            }
            Log.i("DbExampleLog", account.getName().getDisplayName());

            try {
                uploadData(data[0]);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(String result) {
            Toast toast = Toast.makeText(getApplicationContext(), "Done", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}
