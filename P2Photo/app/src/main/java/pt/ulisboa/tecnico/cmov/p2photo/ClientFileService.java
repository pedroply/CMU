package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.TreeMap;

public class ClientFileService extends Service {

    private GlobalClass global;
    private String loginToken, user, host;
    private Socket socket = new Socket();
    private int port = 8888;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        global = (GlobalClass) getApplicationContext();
        loginToken = global.getUserLoginToken();
        user = global.getUserName();

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        host = intent.getStringExtra("host");


        return START_STICKY;
    }


    class DownloadFilesFromServerTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... voids) {
            TreeMap<String, ArrayList<String>> photosAvailable = new TreeMap<String, ArrayList<String>>();
            TreeMap<String, ArrayList<String>> photosToReceive = new TreeMap<String, ArrayList<String>>();
            String usernameHost = "";

            try {
                // Get query.txt from server
                File queryFile = new File(getApplicationContext().getFilesDir() + "/query.txt");
                if (!queryFile.exists()) {
                    queryFile.createNewFile();
                }

                InputStream is = socket.getInputStream();
                copyFile(is, new FileOutputStream(queryFile));

                Scanner scanner = new Scanner(queryFile);
                String currentAlbum = "";

                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if (line.contains("User")) {
                        line = line.replace("User: ", "");
                        usernameHost = line.replace("\n", "");

                    } else if (line.contains("Album")) {
                        line = line.replace("Album: ", "");
                        currentAlbum = line.replace("\n", "");
                        photosAvailable.put(currentAlbum, new ArrayList<String>());

                    } else if (line.contains("Photo")) {
                        line = line.replace("Photo: ", "");
                        line = line.replace("\n", "");
                        ArrayList<String> photosList = photosAvailable.get(currentAlbum);
                        photosList.add(line);
                    }
                }

                // See shared folders with Host's user
                for (String album : photosAvailable.keySet()) {
                    String url = "http://" + WebInterface.IP + "/retriveAlbum?name=" + user + "&token=" + loginToken + "&album=" + album;
                    String response = WebInterface.get(url);

                    JSONObject mainObject = new JSONObject(response);
                    JSONArray linkArray = mainObject.getJSONArray("clients");

                    if (alreadyShared(linkArray, usernameHost)) {

                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    private boolean alreadyShared(JSONArray linkArray, String userName){
        try{
            for (int i = 0; i < linkArray.length(); i++) {
                String client = linkArray.getString(i);
                if (client.equals(userName)) {
                    return true;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
