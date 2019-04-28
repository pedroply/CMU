package pt.ulisboa.tecnico.cmov.p2photo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ServerFileService extends Service {

    private ServerSocket serverSocket;
    private Socket clientUpload, clientDownload;
    private GlobalClass global;
    private String loginToken, user;

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
        new UploadFilesToClientTask().execute();

        return START_STICKY;
    }

    class DownloadFilesFromClientTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            // Receive query.txt from client
            // Get the client username, query server for shared albums
            // Select photos from those albums server does not have locally
            // Send new results.txt with wanted albums and photos
            // Wait for client to start sending photos
            // Get photos from client
            // Store photos locally

            return null;
        }
    }

    @SuppressLint("NewApi")
    class UploadFilesToClientTask extends AsyncTask<Void, Void, Void > {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                serverSocket = new ServerSocket(8888);
                clientUpload = serverSocket.accept();

            } catch (IOException e) {
                e.printStackTrace();
            }

            // If photo already downloaded, then do not download again
            TreeMap<String, ArrayList<String>> photosToSend = new TreeMap<String, ArrayList<String>>();

            try{
                // Build a query txt of all information on local files + user
                String queryPath = getApplicationContext().getFilesDir() + "/query.txt";
                File file = new File(queryPath);
                if(!file.exists()){
                    file.createNewFile();
                }

                try(FileOutputStream out = new FileOutputStream(queryPath,false)) {
                    String write = "";
                    write += "User: " + user + "\n";

                    for(String album : global.getAlbumList()){
                        write += "Album: " + album + "\n";

                        for(String photo : global.getAlbumPhotoNameList(album)){
                            write += "Photo: " + photo + "\n";
                        }
                    }

                    byte[] data = write.getBytes();
                    out.write(data);

                } catch(IOException e){
                    e.printStackTrace();
                }

                OutputStream os = clientUpload.getOutputStream();
                InputStream is = null;
                int len;
                byte buf[]  = new byte[1024];

                ContentResolver cr = getApplicationContext().getContentResolver();
                is = cr.openInputStream(Uri.parse(queryPath));

                while ((len = is.read(buf)) != -1) {
                    os.write(buf, 0, len);
                }
                os.close();

                //Get results.txt from Client
                String resultsPath = getApplicationContext().getFilesDir() + "/results.txt";
                File resultsFile = new File(resultsPath);
                if(!file.exists()){
                    file.createNewFile();
                }

                is = clientUpload.getInputStream();
                copyFile(is, new FileOutputStream(resultsFile));

                Scanner scanner = new Scanner(resultsFile);
                String currentAlbum = "";

                while(scanner.hasNextLine()){
                    String line = scanner.nextLine();
                    if(line.contains("Album")){
                        line = line.replace("Album: ", "");
                        currentAlbum = line.replace("\n", "");
                        photosToSend.put(currentAlbum, new ArrayList<String>());

                    } else if(line.contains("Photo")){
                        line = line.replace("Photo: ", "");
                        line = line.replace("\n", "");
                        ArrayList<String> photosList = photosToSend.get(currentAlbum);
                        photosList.add(line);
                    }

                }

                for(Map.Entry<String, ArrayList<String>> album : photosToSend.entrySet()){
                    ArrayList<String> photos = album.getValue();

                    for(String photo : photos){
                        os = clientUpload.getOutputStream();
                        cr = getApplicationContext().getContentResolver();
                        is = null;
                        is = cr.openInputStream(Uri.parse(getApplicationContext().getFilesDir() + "/" + album + "/" + photo));
                        while ((len = is.read(buf)) != -1) {
                            os.write(buf, 0, len);
                        }

                        is = clientUpload.getInputStream();
                        scanner = new Scanner(is);

                        while(scanner.hasNextLine()){
                            String string = scanner.nextLine();
                            if(string.equals("OK")){
                                continue;
                            } else{
                                // Do something
                            }
                        }

                    }

                }

                os.close();
                clientUpload.close();
                serverSocket.close();

            } catch(IOException e){
                e.printStackTrace();

            } finally {
                if(clientUpload != null){
                    if(clientUpload.isConnected()){
                        try {
                            clientUpload.close();
                            serverSocket.close();
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
