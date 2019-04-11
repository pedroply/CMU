package pt.ulisboa.tecnico.cmov.notp2photo;

import android.app.Application;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.TreeMap;

public class GlobalClass extends Application {

    private UserInfo user;
    private Bitmap selectedPhoto;

    public void createUser(String userName, String loginToken){
        user = new UserInfo(userName, loginToken);
    }

    public String getUserName(){
        return user.getUserName();
    }

    public String getUserLoginToken(){
        return user.getLoginToken();
    }

    public String getUserAccessToken(){
        return user.getAccessToken();
    }

    public boolean albumListEmpty(){
        return user.albumListEmpty();
    }

    public void addUserAlbums(ArrayList<String> albumsList){
        for(String album : albumsList){
            user.addNewAlbum(album);
        }
    }

    public void addNewAlbum(String albumName){
        user.addNewAlbum(albumName);
    }

    public ArrayList<String> getAlbumList(){
        return user.getAlbumList();
    }

    public void setUserAccessToken(String accessToken){
        user.setAccessToken(accessToken);
    }

    public boolean albumPhotosIsEmpty(String album){
        return user.albumPhotosIsEmpty(album);
    }

    public void addPhotosToAlbum(String album, ArrayList<Bitmap> photos){
        user.addPhotosToAlbum(album, photos);
    }

    public ArrayList<Bitmap> getAlbumPhotoList(String album){
        return user.getPhotosList(album);
    }

    public void setSelectedPhoto(Bitmap bitmap){
        selectedPhoto = bitmap;
    }
    public Bitmap getSelectedPhoto(){
        return selectedPhoto;
    }

    public void addPhotoToAlbum(String albumName, Bitmap photo){
        user.addPhotoToAlbum(albumName, photo);
    }

}
