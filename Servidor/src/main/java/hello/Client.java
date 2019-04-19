package hello;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Client {

	@JsonIgnore
    private String passwdHashBase64;
    private String name;
    @JsonIgnore
    private String token;
    private String pubKeyBase64;
    @JsonIgnore
    private ArrayList<String> albums;

    public Client(String passwdHashBase64, String name, String pubKeyBase64) {
        this.passwdHashBase64 = passwdHashBase64;
        this.name = name;
        this.pubKeyBase64 = pubKeyBase64;
        this.albums = new ArrayList<String>();
    }

	public String getPasswdHashBase64() {
		return passwdHashBase64;
	}

	public void setPasswdHashBase64(String passwdHashBase64) {
		this.passwdHashBase64 = passwdHashBase64;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ArrayList<String> getAlbums() {
		return albums;
	}

	public void setAlbums(ArrayList<String> albums) {
		this.albums = albums;
	}
	
	public void addAlbum(String album) {
		this.albums.add(album);
	}
	
	public void rmAlbum(String album) {
		for(String i : albums)
			if(i.equals(album))
				albums.remove(i);
	}
	
	public boolean belongsToAlbum(String album) {
		for(String i : albums)
			if(i.equals(album))
				return true;
		return false;
	}

	public String getPubKeyBase64() {
		return pubKeyBase64;
	}

	public void setPubKeyBase64(String pubKeyBase64) {
		this.pubKeyBase64 = pubKeyBase64;
	}
	
}
