package pt.ulisboa.tecnico.cmov.notp2photo;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInfo {
    private String loginToken;
    private String accessToken;
    private String userName;
    private HashMap<String, ArrayList<Bitmap>> albums = new HashMap<String, ArrayList<Bitmap>>();

    public UserInfo(String user, String loginToken){
        userName = user;
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
        albums.put(albumName, new ArrayList<Bitmap>());
    }

    public synchronized ArrayList<String> getAlbumList(){
        return new ArrayList<String>(albums.keySet());
    }

    public synchronized boolean albumPhotosIsEmpty(String albumName){
        ArrayList<Bitmap> photos = albums.get(albumName);
        return photos.isEmpty();
    }

    public synchronized void addPhotosToAlbum(String albumName, ArrayList<Bitmap> photos){
        albums.put(albumName, photos);
    }

    public synchronized void addPhotoToAlbum(String albumName, Bitmap photo){
        ArrayList<Bitmap> photos = albums.get(albumName);
        photos.add(photo);
    }

    public synchronized ArrayList<Bitmap> getPhotosList(String albumName){
        return albums.get(albumName);
    }


}
