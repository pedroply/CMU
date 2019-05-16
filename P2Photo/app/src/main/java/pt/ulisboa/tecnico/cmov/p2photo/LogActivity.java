package pt.ulisboa.tecnico.cmov.p2photo;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        new getLogs().execute();
    }

    private class getLogs extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String url = "http://" + WebInterface.IP + "/getLog";
            String response = WebInterface.get(url);
            if(response == null)
                return null;
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            if(response == null) {
                Toast.makeText(getApplicationContext(), "Internet Error", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Map<String, String>> logList = new ArrayList<Map<String, String>>();
            try {
                JSONObject mainObject = new JSONObject(response);
                JSONArray logArray = mainObject.getJSONArray("logs");
                for (int i = logArray.length() - 1; i >= 0; i--) {
                    Map<String, String> logMap = new HashMap<String, String>(2);
                    String[] log = logArray.getString(i).split(" - ");
                    logMap.put("title", log[1]);
                    logMap.put("date", log[0]);
                    logList.add(logMap);
                }
                SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), logList, R.layout.activity_log_view,
                        new String[] {"title", "date"}, new int[] {R.id.titleText, R.id.dataText});
                final ListView listView = (ListView) findViewById(R.id.logList);
                listView.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}