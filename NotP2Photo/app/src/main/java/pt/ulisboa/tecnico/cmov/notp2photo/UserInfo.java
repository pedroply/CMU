package pt.ulisboa.tecnico.cmov.notp2photo;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;

public class UserInfo {
    private String loginToken;
    private String accessToken;
    private String userName;
    private HashMap<String, ArrayList<Bitmap>> albums = new HashMap<String, ArrayList<Bitmap>>();

    public UserInfo(String user){
        userName = user;
    }


}
