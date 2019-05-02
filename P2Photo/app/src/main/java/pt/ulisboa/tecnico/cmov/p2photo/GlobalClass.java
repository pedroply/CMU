package pt.ulisboa.tecnico.cmov.p2photo;

import android.app.ActivityManager;
import android.app.Application;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.TreeMap;

public class GlobalClass extends Application {

    private UserInfo user;
    private Bitmap selectedPhoto, servicePhoto;
    private ArrayList<String> eventLog = new ArrayList<String>();
    private ArrayList<String> alreadyDownloaded = new ArrayList<String>();

    public void createUser(String userName, String loginToken){
        user = new UserInfo(userName, loginToken);
    }

    public String getUserName(){
        return user.getUserName();
    }

    public String getUserLoginToken(){
        return user.getLoginToken();
    }

    public boolean albumListEmpty(){
        return user.albumListEmpty();
    }

    public void addUserAlbums(ArrayList<String> albumsList){
        for(String album : albumsList){
            if(!user.containsAlbum(album)){
                user.addNewAlbum(album);
            }
        }
    }

    public void addNewAlbum(String albumName){
        user.addNewAlbum(albumName);
    }

    public ArrayList<String> getAlbumList(){
        return user.getAlbumList();
    }

    public boolean albumPhotosIsEmpty(String album){
        return user.albumPhotosIsEmpty(album);
    }

    public void addPhotosToAlbum(String album, TreeMap<String, Bitmap> photos){
        user.addPhotosToAlbum(album, photos);
    }

    public ArrayList<Bitmap> getAlbumPhotoList(String album){
        return user.getPhotosList(album);
    }

    public ArrayList<String> getAlbumPhotoNameList(String album) {
        ArrayList<String> photos = new ArrayList<String>();
        ArrayList<String> linkList = user.getAlbumPhotoLink(album);

        for(String link : linkList){
            File f = new File(link);
            photos.add(f.getName());
        }

        return photos;
    }

    public void setSelectedPhoto(Bitmap bitmap){
        selectedPhoto = bitmap;
    }

    public Bitmap getSelectedPhoto(){
        return selectedPhoto;
    }

    public void setServicePhoto(Bitmap bitmap){
        servicePhoto = bitmap;
    }

    public Bitmap getServicePhoto(){
        return servicePhoto;
    }

    public void addPhotoToAlbum(String albumName, Bitmap photo, String link){
        user.addPhotoToAlbum(albumName, photo, link);
    }

    public boolean containsPhoto(String album, String link){
        return user.containsPhoto(album, link);
    }

    public void addEvent(String event){
        eventLog.add(event);
    }

    public ArrayList<String> getLogEvent(){
        return eventLog;
    }

    public void addDownload(String album){
        alreadyDownloaded.add(album);
    }

    public boolean isDownloaded(String album){
        return alreadyDownloaded.contains(album);
    }

    public void clearDownloads(){
        alreadyDownloaded.clear();
    }

    public void addNewAlbumShared(String album){
        if(!user.albumAlreadyInShared(album)){
            user.addNewAlbumToShared(album);
        }
    }

    public void addUsersSharedWithAlbum(String album, JSONArray array) throws JSONException {
        ArrayList<String> usersSharedWithMe = new ArrayList<String>();
        for (int i = 0; i < array.length(); i++) {
            String client = array.getString(i);
            if(client.equals(user.getUserName())){
                continue;
            }
            usersSharedWithMe.add(client);
        }

        user.addUsersToSharedAlbum(album, usersSharedWithMe);
    }

    public ArrayList<String> getSharedAlbumUsers(String album){
        return user.getSharedAlbumUserList(album);
    }
}