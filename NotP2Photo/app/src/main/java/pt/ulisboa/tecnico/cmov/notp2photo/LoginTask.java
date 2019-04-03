package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginTask extends AsyncTask<String, Void, String> {

    private Context context;
    private JSONObject mainObject;

    public LoginTask(Context context){
        this.context = context;
    }

    protected String doInBackground(String... urls) {

        String response = WebInterface.get(urls[0]);
        Log.i(MainActivity.TAG, "Response: " + response.toString());
        System.out.println(response.toString());
        return response.toString();
    }

    protected void onPostExecute(String response) {
        // TODO: check this.exception
        // TODO: do something with the feed
        System.out.println("On main: " + response);
        try {
            mainObject = new JSONObject(response);

            if(mainObject.has("token")){
                Toast toast = Toast.makeText(context, "Login Ok! token: " + mainObject.getString("token"), Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                Toast toast = Toast.makeText(context, "Login Error!", Toast.LENGTH_SHORT);
                toast.show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(context, "Login Error!", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public JSONObject getMainObject(){
        return mainObject;
    }
}
