package pt.ulisboa.tecnico.cmov.notp2photo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import java.util.ArrayList;

public class LogActivity extends AppCompatActivity {

    private GlobalClass global;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        global = (GlobalClass) getApplicationContext();

        EditText logText = (EditText) findViewById(R.id.logEditText);
        logText.setText(convertLogToString(global.getLogEvent()));

    }

    private String convertLogToString(ArrayList<String> log){
        String result = "";

        for(String logEvent : log){
            result += logEvent + "\n";
        }

        return result;

    }
}
