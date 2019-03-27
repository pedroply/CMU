package hello;

import java.util.ArrayList;

public class Client {

    private String passwdHashBase64;
    private String name;
    private String token;
    private ArrayList<String> links;
    private ArrayList<String> albums;

    public Client(String passwdHashBase64, String name) {
        this.passwdHashBase64 = passwdHashBase64;
        this.name = name;
        this.links = new ArrayList<String>();
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

	public ArrayList<String> getLinks() {
		return links;
	}

	public void setLinks(ArrayList<String> links) {
		this.links = links;
	}
	
	public void addLink(String link) {
		this.links.add(link);
	}
	
	public void rmLink(String link) {
		for(String i : links)
			if(i.equals(link))
				links.remove(i);
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

    
}
