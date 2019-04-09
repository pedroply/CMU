package hello;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Album {
	private String name;
	//Clients only contribute with a single link to the album
	@JsonIgnore
	private HashMap<String, String> clientsLinks;
	private ArrayList<String> links;
	
	public Album(String name) {
		this.name = name;
		this.clientsLinks = new HashMap<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getClients() {
		ArrayList<String> clients = new ArrayList<>();
		for(String i : clientsLinks.keySet())
			clients.add(i);
		return clients;
	}
	
	public void updateClient(String client, String link) {
		if(clientsLinks.containsKey(client))
			clientsLinks.remove(client);
		clientsLinks.put(client, link);
	}

	public ArrayList<String> getLinks() {
		ArrayList<String> links = new ArrayList<>();
		for(String i : clientsLinks.values())
			links.add(i);
		return links;
	}
	
	
} 
