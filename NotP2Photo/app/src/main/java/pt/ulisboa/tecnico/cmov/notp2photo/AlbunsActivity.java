package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;

public class AlbunsActivity extends AppCompatActivity {

    private static String TAG = "AlbunsActivity";
    private String user;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albuns);

        token = getIntent().getStringExtra("token");
        user = getIntent().getStringExtra("user");

        findViewById(R.id.refreshAlbunsButton).performClick();

        System.out.println("User: " + user + " token: " + token);
    }

    public void newAlbum(View v) {
        // /createAlbum?name=pedro&token=ObVNGg==&album=test

        EditText userTextInput = findViewById(R.id.albumInputText);
        String album = userTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/createAlbum?name="+user+"&token="+token+"&album="+album;
        Log.d(TAG, "URL: " + url);
        System.out.println(url);

        new newAlbumTask().execute(url);
    }

    public void refreshAlbuns(View v) throws IOException {
        // /retriveAllAlbuns?name=qwe&token=BZUGZg==

        String url = "http://" + WebInterface.IP + "/retriveAllAlbuns?name="+user+"&token="+token;
        Log.d(TAG, "URL: " + url);
        System.out.println(url);

        new refreshAlbunsTask().execute(url);
    }

    class newAlbumTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {

            String response = WebInterface.get(urls[0]);
            Log.i(TAG, "Response: " + response.toString());
            System.out.println(response.toString());
            return response.toString();
        }

        protected void onPostExecute(String response) {
            System.out.println("On main: " + response);
            try {
                JSONObject mainObject = new JSONObject(response);
                if(mainObject.has("response") && mainObject.getString("response").equals(WebInterface.responseOK)){
                    Toast toast = Toast.makeText(getApplicationContext(), "New Album Created!", Toast.LENGTH_SHORT);
                    toast.show();
                    findViewById(R.id.refreshAlbunsButton).performClick();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Album Error!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Album Error!", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    class refreshAlbunsTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {

            String response = WebInterface.get(urls[0]);
            Log.i(TAG, "Response: " + response.toString());
            System.out.println(response.toString());
            return response.toString();
        }

        protected void onPostExecute(String response) {
            System.out.println("On main: " + response);
            LinearLayout albumContainer = findViewById(R.id.albumContainer);
            albumContainer.removeAllViews();
            try {
                JSONArray mainObject = new JSONArray(response);
                for(int i = 0; i<mainObject.length(); i++){
                    TextView tmpAlbumView = new TextView(getBaseContext());
                    tmpAlbumView.setTextColor(Color.parseColor("#333333"));
                    tmpAlbumView.setTextSize(25);
                    tmpAlbumView.setText(mainObject.getString(i));

                    tmpAlbumView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(getBaseContext(), AlbumViewActivity.class);
                            intent.putExtra("token", token);
                            intent.putExtra("user", user);
                            intent.putExtra("album", ((TextView)v).getText().toString());
                            startActivity(intent);
                        }
                    });

                    albumContainer.addView(tmpAlbumView);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Album Error!", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

}
