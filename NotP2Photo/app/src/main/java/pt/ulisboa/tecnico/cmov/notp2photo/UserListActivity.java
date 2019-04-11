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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserListActivity extends AppCompatActivity {
    private String token, loginToken, user;
    private Context context = this;
    private int numberOfChecks = 0;
    private Map<String, Boolean> isChecked;
    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        global = (GlobalClass) getApplicationContext();

        token = global.getUserAccessToken();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();

        // Disable button until at least one option is chosen
        changeButton(false, (float) 0.5);
        isChecked = new HashMap<>();

        String url = "http://" + WebInterface.IP + "/retriveUsers?name=" + user + "&token=" + loginToken;
        new userLoader().execute(url);
    }

    private void changeButton(boolean enabled, float alpha) {
        Button button = (Button) findViewById(R.id.invite);
        button.setEnabled(enabled);
        button.setAlpha(alpha);
    }

    public void inviteUsers(View v) {
        List<String> usernames = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : isChecked.entrySet()) {
            if (entry.getValue())
                usernames.add(entry.getKey());
        }
        Intent intent = new Intent(context, ChooseAlbumUserActivity.class);
        intent.putExtra("usernames", usernames.toArray(new String[0]));
        startActivity(intent);
    }

    private class userLoader extends AsyncTask<String, Void, List<String>> {

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
    }
}
