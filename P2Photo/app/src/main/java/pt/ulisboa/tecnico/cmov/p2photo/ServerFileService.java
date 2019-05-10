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
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class ServerFileService extends Service {

    private GlobalClass global;
    private String loginToken, user;
    private static P2PActivity activity;
    private boolean finishedDownload = false, finishedUpload = false;
    private Object lock;

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

    public static void setActivity(P2PActivity p2p){
        activity = p2p;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Toast.makeText(this, "Exchanging photos with peer...", Toast.LENGTH_SHORT).show();

        new UploadFilesToClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new DownloadFilesFromClientTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return START_STICKY;
    }

    @SuppressLint("NewApi")
    class DownloadFilesFromClientTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            TreeMap<String, ArrayList<String>> photosAvailable = new TreeMap<String, ArrayList<String>>();
            TreeMap<String, ArrayList<String>> photosToReceive = new TreeMap<String, ArrayList<String>>();
            String usernameHost;

            try {
                // Get client download Socket
                Socket clientDownload = global.getClientDownloadSocketService();
                if(clientDownload == null){
                    return null;
                }

                // Get client's available photos on his local storage
                usernameHost = extractParametersFromInputStream(photosAvailable, clientDownload);

                // Select photos to receive from the client
                selectPhotosToReceive(photosAvailable, usernameHost, photosToReceive);

                // Parse albums and photos to receive from the client
                String toSend = listOfPhotosToString(photosToReceive);

                // Send parsed String to client
                sendStringToSocket(toSend, clientDownload);


                for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()) {
                    ArrayList<String> photoList = album.getValue();

                    for (String photo : photoList) {
                        // Write received bytes to new bitmap file
                        Bitmap bitmap = writeBitmapToNewLocalFile(album.getKey(), photo, clientDownload);

                        if(bitmap != null){
                            // Send to client "OK" to send new photo
                            sendStringToSocket("OK", clientDownload);

                            global.addPhotoToAlbum(album.getKey(), bitmap, getApplicationContext().getFilesDir() + "/" + album.getKey() + "/" + photo);

                        } else{
                            return null;
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    global.closeDownloadSockets();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return "Received photos from peer";
        }

        @Override
        protected void onPostExecute(String string){
            if(string != null){
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Could not receive photos from peer", Toast.LENGTH_SHORT).show();
            }

            synchronized (lock){
                finishedDownload = true;

                if(finishedDownload && finishedUpload){
                    activity.closeConnection();
                }
            }

        }
    }

    @SuppressLint("NewApi")
    class UploadFilesToClientTask extends AsyncTask<Void, Void, String > {

        @Override
        protected String doInBackground(Void... voids) {
            TreeMap<String, ArrayList<String>> photosToSend = new TreeMap<String, ArrayList<String>>();

            try{
                // Get client upload socket
                Socket clientUpload = global.getClientUploadSocketService();
                if(clientUpload == null){
                    return null;
                }

                // Send my available photos to client
                String toSend = listOfPhotosToString(global.getAlbumsWithPhotoNames());

                // Send information to client socket
                sendStringToSocket(toSend, clientUpload);

                // Receive which photos to send from client
                extractParametersFromInputStream(photosToSend, clientUpload);

                for(Map.Entry<String, ArrayList<String>> album : photosToSend.entrySet()){
                    ArrayList<String> photos = album.getValue();

                    for(String photo : photos){
                        // Send photo bitmap bytes to client
                        sendPhotoToClient(album.getKey(), photo, clientUpload);

                        // Wait for client OK to send next photo
                        Scanner scanner = new Scanner(clientUpload.getInputStream());
                        if(!scanner.hasNextLine()){
                            return null;
                        }
                    }
                }

            } catch(IOException e){
                e.printStackTrace();
                return null;
            } finally {
                try {
                    global.closeUploadSockets();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            return "Exchanged my photos with peer";
        }

        @Override
        protected void onPostExecute(String string){
            if(string != null){
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Could not send my photos to peer", Toast.LENGTH_SHORT).show();
            }

            synchronized (lock){
                finishedUpload = true;

                if(finishedDownload && finishedUpload){
                    activity.closeConnection();
                }
            }
        }

    }

    private String extractParametersFromInputStream(TreeMap<String,ArrayList<String>> photosAvailable, Socket clientSocket) throws IOException {
        String currentAlbum = "", usernameHost = "";
        Scanner scanner = new Scanner(clientSocket.getInputStream());

        if(scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] parse = line.split(" ");

            for(String field : parse){
                if (field.contains("User")) {
                    field = field.replace("User:", "");
                    usernameHost = field;

                } else if (field.contains("Album")) {
                    field = field.replace("Album:", "");
                    currentAlbum = field;
                    photosAvailable.put(currentAlbum, new ArrayList<String>());

                } else if (field.contains("Photo")) {
                    field = field.replace("Photo:", "");
                    ArrayList<String> photosList = photosAvailable.get(currentAlbum);
                    photosList.add(field);
                }
            }
        }

        return usernameHost;

    }

    private void selectPhotosToReceive(TreeMap<String, ArrayList<String>> photosAvailable, String usernameHost, TreeMap<String,ArrayList<String>> photosToReceive){
        for (String album : photosAvailable.keySet()) {
            ArrayList<String> users = global.getSharedAlbumUsers(album);
            if(users == null)
                continue;

            if (users.contains(usernameHost)) {
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
    }

    private String listOfPhotosToString(TreeMap<String,ArrayList<String>> photosToReceive){
        String result = "";
        result += "User:" + user + " ";

        for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()){
            ArrayList<String> photoList = album.getValue();
            result += "Album:" + album.getKey() + " ";

            for(String photo : photoList){
                result += "Photo:" + photo + " ";
            }

        }

        return result;
    }

    private Bitmap writeBitmapToNewLocalFile(String album, String photo, Socket clientSocket) throws IOException {
        File photoFile = new File(getApplicationContext().getFilesDir() + "/" + album + "/" + photo);
        if (!photoFile.exists())
            photoFile.createNewFile();

        Scanner scanner = new Scanner(clientSocket.getInputStream());
        String stringBitmap;

        if(scanner.hasNextLine()){
            stringBitmap = scanner.nextLine();
        } else {
            return null;
        }

        byte[] mybytearray = Base64.decode(stringBitmap, Base64.NO_WRAP);
        Bitmap bitmap = BitmapFactory.decodeByteArray(mybytearray, 0, mybytearray.length);

        FileOutputStream fos = new FileOutputStream(photoFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
        fos.write(mybytearray);
        fos.close();

        return bitmap;
    }

    private void sendStringToSocket(String toSend, Socket clientSocket) throws IOException {
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
        pw.println(toSend);
    }

    private void sendPhotoToClient(String album, String photo, Socket clientSocket) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir() + "/" + album + "/" + photo, options);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] mybytearray = stream.toByteArray();

        String encoded = Base64.encodeToString(mybytearray, Base64.NO_WRAP);

        sendStringToSocket(encoded, clientSocket);
    }
}
