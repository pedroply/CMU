package hello;

import java.util.HashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	//					  name,	  client
	public static HashMap<String, Client> clients = new HashMap<String, Client>();
	//	  				  name,	  album
	public static HashMap<String, Album> albums = new HashMap<String, Album>();
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
