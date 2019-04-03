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

    public static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View v) {
        // /login?name=joao&passwdHashBase64=123
        EditText userTextInput = findViewById(R.id.userInputText);
        String user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/login?name="+user+"&passwdHashBase64="+pass;
        Log.d(TAG, "URL: " + url);
        System.out.println(url);

        LoginTask task = new LoginTask(getApplicationContext());
        task.execute(url);

        try{
            Intent intent = new Intent(MainActivity.this, AlbunsActivity.class);
            intent.putExtra("token", task.getMainObject().getString("token"));
            intent.putExtra("user", user);
            startActivity(intent);
        } catch(JSONException e){
            e.printStackTrace();
        }

    }

    public void register(View v) throws IOException {
        // /register?name=joao&passwdHashBase64=123
        EditText userTextInput = findViewById(R.id.userInputText);
        String user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/register?name="+user+"&passwdHashBase64="+pass;
        Log.d(TAG, "URL: " + url);

        new RegisterTask(getApplicationContext()).execute(url);

    }

}
