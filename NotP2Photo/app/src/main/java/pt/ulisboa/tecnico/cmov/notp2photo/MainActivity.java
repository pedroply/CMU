package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "LoginActivity";

    public static String user;
    public static String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View v) {
        // /login?name=joao&passwdHashBase64=123
        EditText userTextInput = findViewById(R.id.userInputText);
        user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/login?name="+user+"&passwdHashBase64="+pass;
        Log.d(TAG, "URL: " + url);
        System.out.println(url);

        new loginTask().execute(url);
    }

    public void register(View v) throws IOException {
        // /register?name=joao&passwdHashBase64=123
        EditText userTextInput = findViewById(R.id.userInputText);
        String user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/register?name="+user+"&passwdHashBase64="+pass;
        Log.d(TAG, "URL: " + url);

        new registerTask().execute(url);

    }

    public void goToDropbox(View v) {
        Intent intent = new Intent(getBaseContext(), InteractWithDropBox.class);
        startActivity(intent);
    }

    class loginTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {

            String response = WebInterface.get(urls[0]);
            Log.i(TAG, "Response: " + response.toString());
            System.out.println(response.toString());
            return response.toString();
        }

        protected void onPostExecute(String response) {
            // TODO: check this.exception
            // TODO: do something with the feed
            System.out.println("On main: " + response);
            try {
                JSONObject mainObject = new JSONObject(response);
                if(mainObject.has("token")){
                    Toast toast = Toast.makeText(getApplicationContext(), "Login Ok! token: " + mainObject.getString("token"), Toast.LENGTH_SHORT);
                    toast.show();
                    token = mainObject.getString("token");
                    /*Intent intent = new Intent(getBaseContext(), AlbunsActivity.class);
                    startActivity(intent);*/
                    Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                    startActivity(intent);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Login Error!", Toast.LENGTH_SHORT);
                    toast.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Login Error!", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
    }

    class registerTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {

            String response = WebInterface.get(urls[0]);
            Log.i(TAG, "Response: " + response.toString());
            System.out.println(response.toString());
            return response.toString();
        }

        protected void onPostExecute(String response) {
            // TODO: check this.exception
            // TODO: do something with the feed
            System.out.println("On main: " + response);
            try {
                JSONObject mainObject = new JSONObject(response);
                if(mainObject.has("response") && mainObject.getString("response").equals(WebInterface.responseOK)){
                    Toast toast = Toast.makeText(getApplicationContext(), "Register Ok!", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Register Error! " + mainObject.getString("response"), Toast.LENGTH_SHORT);
                    toast.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast toast = Toast.makeText(getApplicationContext(), "Register Error!", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

}
