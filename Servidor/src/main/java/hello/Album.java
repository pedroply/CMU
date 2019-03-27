package hello;

import java.util.ArrayList;

public class Album {
	private String name;
	private ArrayList<String> clients;
	private ArrayList<String> links;
	
	public Album(String name) {
		this.name = name;
		this.clients = new ArrayList<String>();
		this.links = new ArrayList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getClients() {
		return clients;
	}

	public void setClients(ArrayList<String> clients) {
		this.clients = clients;
	}
	
	public void addClient(String name) {
		this.clients.add(name);
	}
	
	public void rmClient(String name) {
		for(String i : clients)
			if(i.equals(name))
				clients.remove(i);
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
	
	
} 
