package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterTask extends AsyncTask<String, Void, String> {

    private Context context;

    public RegisterTask(Context context){
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
            JSONObject mainObject = new JSONObject(response);
            if(mainObject.has("response") && mainObject.getString("response").equals(WebInterface.responseOK)){
                Toast toast = Toast.makeText(context, "Register Ok!", Toast.LENGTH_SHORT);
                toast.show();
            }
            else{
                Toast toast = Toast.makeText(context, "Register Error! " + mainObject.getString("response"), Toast.LENGTH_SHORT);
                toast.show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast toast = Toast.makeText(context, "Register Error!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}