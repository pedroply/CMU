package pt.ulisboa.tecnico.cmov.notp2photo;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;
import java.lang.reflect.Array;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class UserInfo {
    private String loginToken;
    private String accessToken;
    private String userName;
    private KeyPair keyPair;
    private TreeMap<String, TreeMap<String, Bitmap>> albums = new TreeMap<String, TreeMap<String, Bitmap>>();

    public UserInfo(String user, String loginToken, Context c){
        userName = user;
        this.loginToken = loginToken;
        try {
            keyPair = RSAGenerator.readKeysFromFiles(user, c);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getUserName(){
        return userName;
    }

    public String getLoginToken(){
        return loginToken;
    }

    public String getAccessToken(){
        return accessToken;
    }

    public void setAccessToken(String accessToken){
        this.accessToken = accessToken;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    public boolean albumListEmpty(){
        return albums.isEmpty();
    }

    public synchronized void addNewAlbum(String albumName){
        albums.put(albumName, new TreeMap<String, Bitmap>());
    }

    public synchronized ArrayList<String> getAlbumList(){
        return new ArrayList<String>(albums.keySet());
    }

    public synchronized boolean albumPhotosIsEmpty(String albumName){
        TreeMap<String,Bitmap> photos = albums.get(albumName);
        return photos.isEmpty();
    }

    public synchronized void addPhotosToAlbum(String albumName, TreeMap<String, Bitmap> photos){
        albums.put(albumName, photos);
    }

    public synchronized void addPhotoToAlbum(String albumName, Bitmap photo, String url){
        TreeMap<String,Bitmap> photos = albums.get(albumName);
        photos.put(url, photo);
    }

    public synchronized ArrayList<Bitmap> getPhotosList(String albumName){
        TreeMap<String, Bitmap> photos =  albums.get(albumName);
        return new ArrayList<Bitmap>(photos.values());
    }

    public synchronized boolean containsPhoto(String albumName, String link){
        TreeMap<String, Bitmap> photos =  albums.get(albumName);
        return photos.containsKey(link);
    }

    public synchronized boolean containsAlbum(String albumName){
        return albums.containsKey(albumName);
    }


}
