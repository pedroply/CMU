package hello;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Random;
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

    @RequestMapping("/register")
    public String register(@RequestParam String passwdHashBase64, @RequestParam String name) {
    	if (Application.clients.containsKey(name)) {
    		return "{\"response\":\"Client Already Registered\"}";
    	}
    	Application.clients.put(name, new Client(passwdHashBase64, name));
        return "{\"response\":\"OK\"}";
    }
    
    @RequestMapping("/login")
    public String login(@RequestParam String passwdHashBase64, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getPasswdHashBase64().equals(passwdHashBase64)) {
    		String token = randomTokenNotSecure();
    		Application.clients.get(name).setToken(token);
    		return "{\"token\":\""+token+"\"}";
    	}
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/logout")
    public String logout(@RequestParam String token, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		Application.clients.get(name).setToken(null);
    		return "{\"response\":\"OK\"}";
    	}
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/createAlbum")
    public String createAlbum(@RequestParam String token, @RequestParam String name, @RequestParam String album) {
    	System.out.println(Application.clients.get(name).getToken() + " == " + token);
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		if(!Application.albums.containsKey(album)) {
	    		Application.albums.put(album, new Album(album));
	    		Application.albums.get(album).addClient(name);
	    		Application.clients.get(name).addAlbum(album);
	    		return "{\"response\":\"OK\"}";
    		}
    		else {
    			return "{\"response\":\"Album Already Registered\"}";
    		}
    	}
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/addClient2Album")
    public String addClient2Album(@RequestParam String token, @RequestParam String name, @RequestParam String album, @RequestParam String client2Add) {
    	if (Application.clients.containsKey(name) && Application.clients.containsKey(client2Add) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		Application.albums.get(album).addClient(client2Add);
    		Application.clients.get(name).addAlbum(album);
    		return "{\"response\":\"OK\"}";
    	}
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
    public String persistPerson(@RequestParam String token, @RequestParam String name, @RequestParam String album, @RequestBody String link) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		System.out.println("Posting linnk: " + link);
        	System.out.println("To Album: " + album);
    		Application.clients.get(name).addLink(link);
    		Application.albums.get(album).addLink(link);
    		return "{\"response\":\"OK\"}";
    	}
        return "{\"response\":\"ERR\"}";
    }
    
    @RequestMapping("/retriveAlbum")
    public Album getAlbum(@RequestParam String token, @RequestParam String name, @RequestParam String album) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		return Application.albums.get(album);
    	}
        return new Album("null");
    }
    
    @RequestMapping("/retriveAllAlbuns")
    public ArrayList<String> getAlbuns(@RequestParam String token, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		return Application.clients.get(name).getAlbums();
    	}
        return new ArrayList<>();
    }
    
    @RequestMapping("/reset")
    public String reset() {
    	Application.clients = new HashMap<String, Client>();
    	Application.albums = new HashMap<String, Album>();
		return "Done!";
    }
    
    private String randomTokenNotSecure() {
        byte[] array = new byte[4]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);
        System.out.println("Token Generated: " + generatedString);
        return generatedString;
    }
}
