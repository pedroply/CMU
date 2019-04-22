package pt.ulisboa.tecnico.cmov.p2photo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ChooseAlbumActivity extends AppCompatActivity {

    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_album);

        global = (GlobalClass) getApplicationContext();

        setListView(global.getAlbumList());
    }

    private void setListView(ArrayList<String> list){
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_home_album_view, list);

        final ListView listView = (ListView) findViewById(R.id.albumList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String album = (String) listView.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "You selected : " + album, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), AddPhotoActivity.class);
                intent.putExtra("album", album);
                startActivity(intent);

                finish();
            }
        });
    }
}
