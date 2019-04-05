package pt.ulisboa.tecnico.cmov.notp2photo;

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

import java.io.IOException;

public class AlbumViewActivity extends AppCompatActivity {

    private static String TAG = "AlbumViewActivity";
    private String album;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_view);

        album = getIntent().getStringExtra("album");

        TextView albumTextView = findViewById(R.id.albumTextView);
        albumTextView.setText(album);

        findViewById(R.id.refreshLinksButton).performClick();

        System.out.println("User: " + MainActivity.user + " token: " + MainActivity.token + " album: " + album);
    }

    public void postLink(View v) {
        // /postLink?name=pedro&token=ObVNGg==&album=test

        EditText linkTextInput = findViewById(R.id.linkInputText);
        String link = linkTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/postLink?name="+MainActivity.user+"&token="+MainActivity.token+"&album="+album;
        Log.d(TAG, "URL: " + url);
        System.out.println(url);

        new postLinkTask().execute(url, link);
    }

    public void refreshLinks(View v) {
        // /retriveAlbum?name=pedro&token=ObVNGg==&album=test

        String url = "http://" + WebInterface.IP + "/retriveAlbum?name="+MainActivity.user+"&token="+MainActivity.token+"&album="+album;
        Log.d(TAG, "URL: " + url);
        System.out.println(url);

        new refreshLinksTask().execute(url);
    }

    class postLinkTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {

            String response = WebInterface.post(urls[0], urls[1]);
            Log.i(TAG, "Response: " + response.toString());
            System.out.println(response.toString());
            return response.toString();
        }

        protected void onPostExecute(String response) {
            System.out.println("On main: " + response);
            try {
                JSONObject mainObject = new JSONObject(response);
                if(mainObject.has("response") && mainObject.getString("response").equals(WebInterface.responseOK)){
                    Toast toast = Toast.makeText(getApplicationContext(), "New Link Posted!", Toast.LENGTH_SHORT);
                    toast.show();
                    findViewById(R.id.refreshLinksButton).performClick();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Link Error!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Link Error!", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    class refreshLinksTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {

            String response = WebInterface.get(urls[0]);
            Log.i(TAG, "Response: " + response.toString());
            System.out.println(response.toString());
            return response.toString();
        }

        protected void onPostExecute(String response) {
            System.out.println("On main: " + response);
            LinearLayout linkContainer = findViewById(R.id.linkContainer);
            linkContainer.removeAllViews();
            try {
                JSONObject mainObject = new JSONObject(response);
                JSONArray linksArray = mainObject.getJSONArray("links");
                for(int i = 0; i<linksArray.length(); i++){
                    TextView tmpLinkView = new TextView(getBaseContext());
                    tmpLinkView.setTextColor(Color.parseColor("#0061ff"));
                    tmpLinkView.setTextSize(15);
                    tmpLinkView.setText(linksArray.getString(i));
                    linkContainer.addView(tmpLinkView);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Album Error!", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

}
