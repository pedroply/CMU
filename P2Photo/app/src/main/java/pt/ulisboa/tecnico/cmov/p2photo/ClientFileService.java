package pt.ulisboa.tecnico.cmov.p2photo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        host = intent.getStringExtra("host");

        new DownloadFilesFromServerTask().execute();
        return START_STICKY;
    }


    @SuppressLint("NewApi")
    class DownloadFilesFromServerTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)));

            } catch (IOException e) {
                e.printStackTrace();
            }

            TreeMap<String, ArrayList<String>> photosAvailable = new TreeMap<String, ArrayList<String>>();
            TreeMap<String, ArrayList<String>> photosToReceive = new TreeMap<String, ArrayList<String>>();
            String usernameHost = "";

            try {
                // Get query.txt from server
                File queryFile = new File(getApplicationContext().getFilesDir() + "/query.txt");
                if (!queryFile.exists()) {
                    queryFile.createNewFile();
                }

                Scanner scanner = new Scanner(socket.getInputStream());
                String encodedString = scanner.nextLine();
                byte[] mybytearray = Base64.decode(encodedString, Base64.NO_WRAP);
                FileOutputStream fos = new FileOutputStream(queryFile);
                fos.write(mybytearray);
                fos.close();

                scanner = new Scanner(queryFile);
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
                        ArrayList<String> photos = photosAvailable.get(album);
                        ArrayList<String> photosMissing = new ArrayList<String>();

                        for(String photo : photos){
                            File photoFile = new File(getApplicationContext().getFilesDir() + "/" + album + "/" + photo);
                            photoFile.delete();
                            if(!photoFile.exists()){
                                photosMissing.add(photo);
                            }
                        }

                        if(photosMissing.size() > 0){
                            photosToReceive.put(album, photosMissing);
                        }
                    }
                }

                String resultsPath = getApplicationContext().getFilesDir() + "/results.txt";
                File file = new File(resultsPath);
                if(!file.exists()){
                    file.createNewFile();
                }

                try(FileOutputStream out = new FileOutputStream(resultsPath,false)) {
                    String write = "";

                    for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()){
                        ArrayList<String> photoList = album.getValue();
                        write += "Album: " + album.getKey() + "\n";

                        for(String photo : photoList){
                            write += "Photo: " + photo + "\n";
                        }

                    }

                    byte[] data = write.getBytes();
                    out.write(data);

                } catch(IOException e){
                    e.printStackTrace();
                }

                mybytearray = new byte [(int)file.length()];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);

                PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
                String encoded = Base64.encodeToString(mybytearray, Base64.NO_WRAP);
                pw.println(encoded);

                for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()){
                    ArrayList<String> photoList = album.getValue();

                    for(String photo : photoList){
                        File photoFile = new File(getApplicationContext().getFilesDir() + "/" + album + "/" + photo);
                        photoFile.createNewFile();
                        scanner = new Scanner(socket.getInputStream());
                        encodedString = scanner.nextLine();
                        mybytearray = Base64.decode(encodedString, Base64.NO_WRAP);
                        fos = new FileOutputStream(photoFile);
                        fos.write(mybytearray);
                        fos.close();

                        pw = new PrintWriter(socket.getOutputStream(), true);
                        pw.println("OK");
                    }

                }

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

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
