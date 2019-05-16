package pt.ulisboa.tecnico.cmov.notp2photo;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.TreeMap;

public class UserInfo {
    private String loginToken, accessToken, userName;
    private TreeMap<String, TreeMap<String, Bitmap>> albums = new TreeMap<String, TreeMap<String, Bitmap>>();

    public UserInfo(String user, String loginToken){
        this.userName = user;
        this.loginToken = loginToken;
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

    public boolean albumListEmpty(){
        return albums.isEmpty();
    }

    public synchronized void addNewAlbum(String albumName){
        albums.put(albumName, new TreeMap<String, Bitmap>());
    }

    public synchronized ArrayList<String> getAlbumList(){
        return new ArrayList<String>(albums.keySet());
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
