package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {
    private String loginToken, user;
    private Context context = this;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        global = (GlobalClass) getApplicationContext();

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
            if(serverResponse == null)
                return null;
            try {
                JSONArray mainObject = new JSONArray(serverResponse);
                for(int i = 0; i < mainObject.length(); i++){
                    String username = mainObject.getString(i);
                    if (!username.equalsIgnoreCase(user)) {
                        userList.add(username);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return userList;
        }

        @Override
        protected void onPostExecute(List<String> list) {
            if(list == null) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_user_list_view, list);

            final ListView listView = (ListView) findViewById(R.id.userList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String userName = (String) listView.getItemAtPosition(position);

                    Intent intent = new Intent(context, ChooseAlbumUserActivity.class);
                    intent.putExtra("user", userName);
                    startActivity(intent);

                    finish();
                }

            });

        }

    }
}
