package hello;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Album {
	private String name;
	//Clients only contribute with a single link to the album
	@JsonIgnore
	private HashMap<String, String> clientsLinks;
	@JsonIgnore
	private HashMap<String, String> clientsIvs;
	private ArrayList<ArrayList<String>> linksIvs;
	private ArrayList<String> clients;
	private HashMap<String, String> encriptedKeys;

	
	public Album(String name) {
		this.name = name;
		this.clientsLinks = new HashMap<>();
		this.clientsIvs = new HashMap<>();
		this.encriptedKeys = new HashMap<>();
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
	
	public void updateClient(String client, String link, String ivBase64) {
		if(clientsLinks.containsKey(client)) {
			clientsLinks.remove(client);
			clientsIvs.remove(client);
		}
		clientsLinks.put(client, link);
		clientsIvs.put(client, ivBase64);
	}
	
	public void updateClient(String client, String encriptedKey) {
		if(clientsLinks.containsKey(client))
			clientsLinks.remove(client);
		clientsLinks.put(client, null);
		encriptedKeys.put(client, encriptedKey);
	}

	public ArrayList<ArrayList<String>> getLinksIvs() {
		ArrayList<ArrayList<String>> linksIvs = new ArrayList<>();
		for(String i : clientsLinks.keySet()) {
			ArrayList<String> linkIv = new ArrayList<>();
			linkIv.add(clientsLinks.get(i));
			linkIv.add(clientsIvs.get(i));
			linkIv.add(i);
			linksIvs.add(linkIv);
		}
		return linksIvs;
	}

	public HashMap<String, String> getEncriptedKeys() {
		return encriptedKeys;
	}

	public void setEncriptedKeys(HashMap<String, String> encriptedKeys) {
		this.encriptedKeys = encriptedKeys;
	}
	
} 
