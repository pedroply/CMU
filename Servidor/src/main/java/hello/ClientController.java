package hello;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(@RequestParam String passwdHashBase64, @RequestParam String name, @RequestBody String pubKeyBase64) {
    	//Log.getInstance().addEntry("Register client: " + name + " with passhash: " + passwdHashBase64);
    	if (Application.clients.containsKey(name)) {
    		Log.getInstance().addEntry("Client " + name + " Already Registered");
    		return "{\"response\":\"Client Already Registered\"}";
    	}
    	Application.clients.put(name, new Client(passwdHashBase64, name, pubKeyBase64));
    	Log.getInstance().addEntry("Client " + name + " Registered Successfully with passhash: " + passwdHashBase64 + " and pubKeyBase64: " + pubKeyBase64);
        return "{\"response\":\"OK\"}";
    }
    
    @RequestMapping("/login")
    public String login(@RequestParam String passwdHashBase64, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getPasswdHashBase64().equals(passwdHashBase64)) {
    		String token = randomTokenNotSecure();
    		Application.clients.get(name).setToken(token);
    		Log.getInstance().addEntry("Login client: " + name + " with passhash: " + passwdHashBase64 + " with token: " + token);
    		return "{\"token\":\""+token+"\"}";
    	}
    	Log.getInstance().addEntry("Login Err client: " + name + " with passhash: " + passwdHashBase64);
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/logout")
    public String logout(@RequestParam String token, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		Application.clients.get(name).setToken(null);
    		Log.getInstance().addEntry("Logout client: " + name + " with token: " + token);
    		return "{\"response\":\"OK\"}";
    	}
    	Log.getInstance().addEntry("Logout Err client: " + name + " with token: " + token);
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping(value = "/createAlbum", method = RequestMethod.POST)
    public String createAlbum(@RequestParam String token, @RequestParam String name, @RequestParam String album, @RequestBody String encriptedKeyBase64) {
    	System.out.println(Application.clients.get(name).getToken() + " == " + token);
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		if(!Application.albums.containsKey(album)) {
	    		Application.albums.put(album, new Album(album));
	    		Application.albums.get(album).updateClient(name, encriptedKeyBase64);
	    		Application.clients.get(name).addAlbum(album);
	    		Log.getInstance().addEntry("Create Album by client: " + name + " with token: " + token + " album name: " + album + " with encriptedKey: " + encriptedKeyBase64);
	    		return "{\"response\":\"OK\"}";
    		}
    		else {
    			Log.getInstance().addEntry("Create Album Err (album already exists) by client: " + name + " with token: " + token + " album name: " + album);
    			return "{\"response\":\"Album Already Registered\"}";
    		}
    	}
    	Log.getInstance().addEntry("Create Album Err by client: " + name + " with token: " + token + " album name: " + album);
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/addClient2Album")
    public String addClient2Album(@RequestParam String token, @RequestParam String name, @RequestParam String album, @RequestParam String client2Add) {
    	if (Application.clients.containsKey(name) && Application.clients.containsKey(client2Add) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		Application.albums.get(album).updateClient(client2Add, null);
    		Application.clients.get(client2Add).addAlbum(album);
    		Log.getInstance().addEntry("Add Client to Album by client: " + name + " with token: " + token + " album name: " + album + " added client name: " + client2Add);
    		return "{\"response\":\"OK\"}";
    	}
		Log.getInstance().addEntry("Add Client to Album Err by client: " + name + " with token: " + token + " album name: " + album + " added client name: " + client2Add);
        return "{\"response\":\"ERR\"}";
    }
    
    /*@RequestMapping("/postLink")
    public String postLink(@RequestParam String token, @RequestParam String name, @RequestParam String link, @RequestParam String album) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		System.out.println("Posting linnk: " + link);
        	System.out.println("To Album: " + album);
    		Application.clients.get(name).addLink(link);
    		Application.albums.get(album).addLink(link);
    		return "{\"response\":\"OK\"}";
    	}
        return "{\"response\":\"ERR\"}";
    }*/
    
    @RequestMapping(value = "/postLink", method = RequestMethod.POST)
    public String postLink(@RequestParam String token, @RequestParam String name, @RequestParam String album, @RequestBody String body) {
    	String link = body.split("\\r?\\n")[0];
    	String ivBase64 = body.split("\\r?\\n")[1];
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		System.out.println("Posting linnk: " + link);
        	System.out.println("To Album: " + album);
    		Application.albums.get(album).updateClient(name, link, ivBase64);
    		Log.getInstance().addEntry("Post Link to Album by client: " + name + " with token: " + token + " album name: " + album + " added link: " + link);
    		return "{\"response\":\"OK\"}";
    	}
		Log.getInstance().addEntry("Post Link to Album Err by client: " + name + " with token: " + token + " album name: " + album + " added link: " + link);
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/retriveAlbum")
    public Album getAlbum(@RequestParam String token, @RequestParam String name, @RequestParam String album) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		Log.getInstance().addEntry("Get Album by client: " + name + " with token: " + token + " album name: " + album);
    		return Application.albums.get(album);
    	}
    	Log.getInstance().addEntry("Get Album Err by client: " + name + " with token: " + token + " album name: " + album);
        return new Album("null");
    }
    
    @RequestMapping("/retriveAllAlbuns")
    public ArrayList<String> getAlbuns(@RequestParam String token, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		Log.getInstance().addEntry("Get Albums by client: " + name + " with token: " + token);
    		return Application.clients.get(name).getAlbums();
    	}
		Log.getInstance().addEntry("Get Albums Err by client: " + name + " with token: " + token);
        return new ArrayList<>();
    }
    
    @RequestMapping("/retriveUsers")
    public Collection<Client> getUsers(@RequestParam String token, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		Log.getInstance().addEntry("Get Clients by client: " + name + " with token: " + token);
    		return Application.clients.values();
    	}
		Log.getInstance().addEntry("Get Clients Err by client: " + name + " with token: " + token);
        return (new HashMap<String, Client>()).values();
    }
    
    @RequestMapping("/getLog")
    public Log getLog() {
    	return Log.getInstance();
    }
    
    @RequestMapping("/reset")
    public String reset() {
    	Application.clients = new HashMap<String, Client>();
    	Application.albums = new HashMap<String, Album>();
		Log.getInstance().addEntry("reset!");
		return "Done!";
    }
    
    @RequestMapping(value = "/test", method = RequestMethod.POST)
    public String test(@RequestParam String urlParam, @RequestBody String body) {
    	String link = body.split("\\r?\\n")[0];
    	String ivBase64 = body.split("\\r?\\n")[1];
    	return "url: " + urlParam + " link: " + link + " ivBase64: " + ivBase64;
    }
    
    private String randomTokenNotSecure() {
        byte[] array = new byte[4]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);
        while(generatedString.contains("+")) {
        	new Random().nextBytes(array);
        	generatedString = Base64.getEncoder().encodeToString(array);
        }
        System.out.println("Token Generated: " + generatedString);
        return generatedString;
    }
}
