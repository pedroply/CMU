package pt.ulisboa.tecnico.cmov.p2photo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ServerFileService extends Service {

    private ServerSocket serverSocket, serverSocketDownload;
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
        Toast.makeText(this, "Exchanging photos with peer...", Toast.LENGTH_SHORT).show();

        new UploadFilesToClientTask().execute();
        new DownloadFilesFromClientTask().execute();

        return START_STICKY;
    }

    @SuppressLint("NewApi")
    class DownloadFilesFromClientTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {

            try {
                serverSocketDownload = new ServerSocket(8889);
                clientDownload = serverSocketDownload.accept();

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

                Scanner scanner = new Scanner(clientDownload.getInputStream());
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

                    if (alreadyShared(linkArray, user)) {
                        ArrayList<String> photos = photosAvailable.get(album);
                        ArrayList<String> photosMissing = new ArrayList<String>();

                        for(String photo : photos){
                            File photoFile = new File(getApplicationContext().getFilesDir() + "/" + album + "/" + photo);
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

                PrintWriter pw = new PrintWriter(clientDownload.getOutputStream(), true);
                String encoded = Base64.encodeToString(mybytearray, Base64.NO_WRAP);
                pw.println(encoded);

                for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()){
                    ArrayList<String> photoList = album.getValue();

                    for(String photo : photoList){
                        File photoFile = new File(getApplicationContext().getFilesDir() + "/" + album.getKey() + "/" + photo);
                        if(!photoFile.exists())
                            photoFile.createNewFile();

                        scanner = new Scanner(clientDownload.getInputStream());
                        encodedString = scanner.nextLine();
                        mybytearray = Base64.decode(encodedString, Base64.NO_WRAP);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(mybytearray, 0, mybytearray.length);

                        fos = new FileOutputStream(photoFile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);

                        fos.write(mybytearray);
                        fos.close();

                        pw = new PrintWriter(clientDownload.getOutputStream(), true);
                        pw.println("OK");
                    }

                }

                clientDownload.close();
                serverSocketDownload.close();

                return "OK";

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (clientDownload != null) {
                    if (clientDownload.isConnected()) {
                        try {
                            clientDownload.close();
                            serverSocketDownload.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(String string){
            if(string != null){
                Toast.makeText(getApplicationContext(), "Received photos from peer", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Could not receive photos from peer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("NewApi")
    class UploadFilesToClientTask extends AsyncTask<Void, Void, String > {

        @Override
        protected String doInBackground(Void... voids) {
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

                byte [] mybytearray = new byte [(int)file.length()];
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);

                PrintWriter pw = new PrintWriter(clientUpload.getOutputStream(), true);
                String encoded = Base64.encodeToString(mybytearray, Base64.NO_WRAP);
                pw.println(encoded);

                //Get results.txt from Client
                String resultsPath = getApplicationContext().getFilesDir() + "/results.txt";
                File resultsFile = new File(resultsPath);
                if(!file.exists()){
                    file.createNewFile();
                }

                Scanner scanner = new Scanner(clientUpload.getInputStream());
                String encodedString = scanner.nextLine();
                mybytearray = Base64.decode(encodedString, Base64.DEFAULT);
                FileOutputStream fos = new FileOutputStream(resultsFile);
                fos.write(mybytearray);
                fos.close();

                scanner = new Scanner(resultsFile);
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
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir() + "/" + album.getKey() + "/" + photo, options);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                        mybytearray = stream.toByteArray();

                        pw = new PrintWriter(clientUpload.getOutputStream(), true);
                        encoded = Base64.encodeToString(mybytearray, Base64.NO_WRAP);
                        pw.println(encoded);

                        InputStream is = clientUpload.getInputStream();
                        scanner = new Scanner(is);

                        while(scanner.hasNextLine()){
                            String string = scanner.nextLine();
                            if(string.equals("OK")){
                                break;
                            } else{
                                // Do something
                            }
                        }

                    }

                }

                clientUpload.close();
                serverSocket.close();

                return "OK";

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

        @Override
        protected void onPostExecute(String string){
            if(string != null){
                Toast.makeText(getApplicationContext(), "Exchanged my photos with peer", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Could not send my photos to peer", Toast.LENGTH_SHORT).show();
            }
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
