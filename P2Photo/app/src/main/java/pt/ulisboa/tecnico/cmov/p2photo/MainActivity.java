package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    protected static String TAG = "LoginActivity";

    private String user;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        global = (GlobalClass) getApplicationContext();
    }

    public void login(View v) {
        EditText userTextInput = findViewById(R.id.userInputText);
        user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/login?name="+user+"&passwdHashBase64="+pass;
        new loginTask().execute(url);
    }

    public void register(View v) {
        EditText userTextInput = findViewById(R.id.userInputText);
        user = userTextInput.getText().toString();

        EditText passTextInput = findViewById(R.id.passInputText);
        String pass = passTextInput.getText().toString();

        String url = "http://" + WebInterface.IP + "/register?name="+user+"&passwdHashBase64="+pass;
        new registerTask().execute(url);

    }

    class loginTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            return WebInterface.get(urls[0]);
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                return;
            }

            System.out.println("On main: " + response);
            try {
                JSONObject mainObject = new JSONObject(response);
                if(mainObject.has("token")){
                    Toast.makeText(getApplicationContext(), "Login Ok!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                    global.createUser(user, mainObject.getString("token"));
                    startActivity(intent);

                }
                else{
                    Toast.makeText(getApplicationContext(), "Login Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Login Error!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    class registerTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            return WebInterface.get(urls[0]);
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                return;
            }

            System.out.println("On main: " + response);
            try {
                JSONObject mainObject = new JSONObject(response);
                if(mainObject.has("response") && mainObject.getString("response").equals(WebInterface.responseOK)){
                    Toast.makeText(getApplicationContext(), "Register Ok!", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Register Error! " + mainObject.getString("response"), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Register Error!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
