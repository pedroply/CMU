package pt.ulisboa.tecnico.cmov.p2photo;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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
        Toast.makeText(this, "Exchanging photos with peer...", Toast.LENGTH_SHORT).show();

        new DownloadFilesFromServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new UploadFilesToServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_STICKY;
    }


    @SuppressLint("NewApi")
    class DownloadFilesFromServerTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            Socket socket;

            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, global.downloadPort)));

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            TreeMap<String, ArrayList<String>> photosAvailable = new TreeMap<String, ArrayList<String>>();
            TreeMap<String, ArrayList<String>> photosToReceive = new TreeMap<String, ArrayList<String>>();
            String usernameHost = "";

            try {
                // Get available photos from server
                usernameHost = extractParametersFromInputStream(photosAvailable, socket);

                // Select which photos to receive from the server
                selectPhotosToReceive(photosAvailable, usernameHost, photosToReceive);

                // Send information of photos to receive to the server
                String toSend = listOfPhotosToString(photosToReceive);
                sendStringToSocket(toSend, socket);

                for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()){
                    ArrayList<String> photoList = album.getValue();

                    for(String photo : photoList){
                        // Write received bytes to new bitmap file
                        Bitmap bitmap = writeBitmapToNewLocalFile(album.getKey(), photo, socket);

                        if(bitmap != null){
                            // Send to server "OK" to send new photo
                            sendStringToSocket("OK", socket);

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
                if (socket != null) {
                    if (!socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
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
        }
    }

    @SuppressLint("NewApi")
    class UploadFilesToServerTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            Socket socket;

            try {
                socket = new Socket();
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, global.uploadPort)));

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            TreeMap<String, ArrayList<String>> photosToSend = new TreeMap<String, ArrayList<String>>();

            try{
                // Send my available photos to server
                String toSend = listOfPhotosToString(global.getAlbumsWithPhotoNames());

                // Send information to client socket
                sendStringToSocket(toSend, socket);

                // Receive which photos to send from server
                extractParametersFromInputStream(photosToSend, socket);

                for(Map.Entry<String, ArrayList<String>> album : photosToSend.entrySet()){
                    ArrayList<String> photos = album.getValue();

                    for(String photo : photos){

                        // Send photo bitmap bytes to client
                        sendPhotoToClient(album.getKey(), photo, socket);

                        // Wait for client OK to send next photo
                        Scanner scanner = new Scanner(socket.getInputStream());
                        if(!scanner.hasNextLine()){
                            return null;
                        }
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
                return null;

            } finally {
                if(socket != null){
                    if(!socket.isClosed()){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
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
        }
    }

    private String extractParametersFromInputStream(TreeMap<String,ArrayList<String>> photosAvailable, Socket clientSocket) throws IOException {
        String currentAlbum = "", usernameHost = "";
        Scanner scanner = new Scanner(clientSocket.getInputStream());

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

        return usernameHost;

    }

    private void selectPhotosToReceive(TreeMap<String, ArrayList<String>> photosAvailable, String usernameHost, TreeMap<String,ArrayList<String>> photosToReceive){
        for (String album : photosAvailable.keySet()) {
            ArrayList<String> users = global.getSharedAlbumUsers(album);

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
        result += "User: " + user + "\n";

        for(Map.Entry<String, ArrayList<String>> album : photosToReceive.entrySet()){
            ArrayList<String> photoList = album.getValue();
            result += "Album: " + album.getKey() + "\n";

            for(String photo : photoList){
                result += "Photo: " + photo + "\n";
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

        byte[] mybytearray = stringBitmap.getBytes();
        Bitmap bitmap = BitmapFactory.decodeByteArray(mybytearray, 0, mybytearray.length);

        FileOutputStream fos = new FileOutputStream(photoFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, fos);
        fos.write(mybytearray);
        fos.close();

        return bitmap;
    }

    private void sendStringToSocket(String toSend, Socket clientSocket) throws IOException {
        PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), false);
        pw.print(toSend);
        pw.flush();
    }

    private void sendPhotoToClient(String album, String photo, Socket clientSocket) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(getApplicationContext().getFilesDir() + "/" + album + "/" + photo, options);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] mybytearray = stream.toByteArray();

        sendStringToSocket(new String(mybytearray), clientSocket);
    }
}
