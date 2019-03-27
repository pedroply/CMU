package hello;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientController {

    @RequestMapping("/register")
    public String register(@RequestParam String passwdHashBase64, @RequestParam String name) {
    	if (Application.clients.containsKey(name)) {
    		return "Client Already Registered";
    	}
    	Application.clients.put(name, new Client(passwdHashBase64, name));
        return "OK";
    }
    
    @RequestMapping("/login")
    public String login(@RequestParam String passwdHashBase64, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getPasswdHashBase64().equals(passwdHashBase64)) {
    		String token = randomTokenNotSecure();
    		Application.clients.get(name).setToken(token);
    		return token;
    	}
        return "ERR";
    }
    
    @RequestMapping("/logout")
    public String logout(@RequestParam String token, @RequestParam String name) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		Application.clients.get(name).setToken(null);
    		return "OK";
    	}
        return "ERR";
    }
    
    @RequestMapping("/createAlbum")
    public String createAlbum(@RequestParam String token, @RequestParam String name, @RequestParam String album) {
    	System.out.println(Application.clients.get(name).getToken() + " == " + token);
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token) && !Application.albums.containsKey(album)) {
    		Application.albums.put(album, new Album(album));
    		Application.albums.get(album).addClient(name);
    		Application.clients.get(name).addAlbum(album);
    		return "OK";
    	}
        return "ERR";
    }
    
    @RequestMapping("/addClient2Album")
    public String addClient2Album(@RequestParam String token, @RequestParam String name, @RequestParam String album, @RequestParam String client2Add) {
    	if (Application.clients.containsKey(name) && Application.clients.containsKey(client2Add) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		Application.albums.get(album).addClient(client2Add);
    		Application.clients.get(name).addAlbum(album);
    		return "OK";
    	}
        return "ERR";
    }
    
    @RequestMapping("/postLink")
    public String postLink(@RequestParam String token, @RequestParam String name, @RequestParam String link, @RequestParam String album) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token) && Application.clients.get(name).belongsToAlbum(album)) {
    		System.out.println("Posting linnk: " + link);
        	System.out.println("To Album: " + album);
    		Application.clients.get(name).addLink(link);
    		Application.albums.get(album).addLink(link);
    		return "OK";
    	}
        return "ERR";
    }
    
    @RequestMapping("/retrive")
    public ArrayList<String> getAlbum(@RequestParam String token, @RequestParam String name, @RequestParam String album) {
    	if (Application.clients.containsKey(name) && Application.clients.get(name).getToken().equals(token)) {
    		return Application.albums.get(album).getLinks();
    	}
        return new ArrayList<String>();
    }
    
    private String randomTokenNotSecure() {
        byte[] array = new byte[4]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = Base64.getEncoder().encodeToString(array);
        System.out.println("Token Generated: " + generatedString);
        return generatedString;
    }
}
