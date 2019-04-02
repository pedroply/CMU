package pt.ulisboa.tecnico.cmov.notp2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "LoginActivity";
    private static String IP = "193.136.128.103:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View v) throws IOException {
        // /login?name=joao&passwdHashBase64=123
        EditText userTextInput = findViewById(R.id.userInputText);
        String user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + IP + "/login?name="+user+"&passwdHashBase64="+pass;
        Log.d(TAG, "URL: " + url);

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Log.i(TAG, "Response: " + response.toString());

    }


}
