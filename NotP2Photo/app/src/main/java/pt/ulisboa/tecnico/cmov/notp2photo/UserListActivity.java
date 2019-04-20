package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity {
    private String token, loginToken, user;
    private Context context = this;
    private GlobalClass global;
    private HashMap<String, String> usersPubKeysBase64 = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        global = (GlobalClass) getApplicationContext();

        token = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();

        String url = "http://" + WebInterface.IP + "/retriveUsers?name=" + user + "&token=" + loginToken;
        new UserLoader().execute(url);
    }

    private class UserLoader extends AsyncTask<String, Void, List<String>>{

        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> userList = new ArrayList<>();
            String serverResponse = WebInterface.get(urls[0]);
            try {
                JSONArray mainObject = new JSONArray(serverResponse);
                for(int i = 0; i < mainObject.length(); i++){
                    String username = mainObject.getJSONObject(i).getString("name");
                    if (!username.equalsIgnoreCase(user)) {
                        userList.add(username);
                        usersPubKeysBase64.put(username, mainObject.getJSONObject(i).getString("pubKeyBase64"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return userList;
        }

        @Override
        protected void onPostExecute(List<String> list) {
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_user_list_view, list);

            final ListView listView = (ListView) findViewById(R.id.userList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String userName = (String) listView.getItemAtPosition(position);

                    Intent intent = new Intent(context, ChooseAlbumUserActivity.class);
                    intent.putExtra("user", userName);
                    intent.putExtra("pubKeyBase64", usersPubKeysBase64.get(userName));
                    startActivity(intent);

                    finish();
                }

            });

        }

    }


   /* private class userLoader extends AsyncTask<String, Void, List<String>> {

        @Override
        protected List<String> doInBackground(String... urls) {
            List<String> userList = new ArrayList<>();
            String serverResponse = WebInterface.get(urls[0]);
            try {
                JSONArray mainObject = new JSONArray(serverResponse);
                for(int i = 0; i < mainObject.length(); i++){
                    String username = mainObject.getString(i);
                    if (!username.equalsIgnoreCase(user)) {
                        userList.add(username);
                        isChecked.put(username, false);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return userList;
        }

        @Override
        protected void onPostExecute(List<String> list) {
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_user_list_view, list);

            final ListView listView = (ListView) findViewById(R.id.userList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.userName);
                    if (textView.isChecked()) {
                        textView.setCheckMarkDrawable(0);
                        textView.setChecked(false);
                        isChecked.put(textView.getText().toString(), false);
                        numberOfChecks--;
                        if (numberOfChecks == 0) {
                            changeButton(false, (float) 0.5);
                        }
                    } else {
                        textView.setCheckMarkDrawable(R.drawable.ic_check);
                        textView.setChecked(true);
                        isChecked.put(textView.getText().toString(), true);
                        numberOfChecks++;
                        if (numberOfChecks == 1) {
                            changeButton(true, (float) 1);
                        }
                    }
                }
            });
        }
    } */
}
