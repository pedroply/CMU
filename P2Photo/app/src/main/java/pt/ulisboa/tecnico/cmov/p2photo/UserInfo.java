package pt.ulisboa.tecnico.cmov.p2photo;

import android.graphics.Bitmap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

public class UserInfo {
    private String loginToken;
    private String userName;
    private TreeMap<String, TreeMap<String, Bitmap>> albums = new TreeMap<String, TreeMap<String, Bitmap>>();
    private TreeMap<String,ArrayList<String>> albumsSharedWithMe = new TreeMap<String,ArrayList<String>>();

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

    public ArrayList<String> getAlbumPhotoLink(String albumName){
        TreeMap<String, Bitmap> photos =  albums.get(albumName);
        return new ArrayList<String>(photos.keySet());
    }

    public void addNewAlbumToShared(String albumName){
        albumsSharedWithMe.put(albumName, new ArrayList<String>());
    }

    public void addUsersToSharedAlbum(String albumName, ArrayList<String> users){
        albumsSharedWithMe.put(albumName, users);
    }

    public boolean albumAlreadyInShared(String albumName){
        return albumsSharedWithMe.containsKey(albumName);
    }

    public ArrayList<String> getSharedAlbumUserList(String albumName){
        return albumsSharedWithMe.get(albumName);
    }
}
