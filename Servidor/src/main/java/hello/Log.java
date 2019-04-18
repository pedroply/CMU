package hello;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Log {
	@JsonIgnore
	private static Log log_instance = null;
	
	
	private TreeSet<String> logs = new TreeSet<>();
	
	
	private Log() {
		
	}
	
	public static Log getInstance() {
		if(log_instance == null) {
			log_instance = new Log();
		}
		return log_instance;
	}
	
	public void addEntry(String entry) {
		String pattern = "MM/dd/yyyy HH:mm:ss";
		DateFormat df = new SimpleDateFormat(pattern);
		Date now = new Date();
		
		logs.add(df.format(now) + " - " + entry);
		System.out.println(retriveLastEntry());
	}
	
	public String retriveLastEntry() {
		return logs.last();
	}

	public TreeSet<String> getLogs() {
		return logs;
	}

	public void setLogs(TreeSet<String> logs) {
		this.logs = logs;
	}
	
}
