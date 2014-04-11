package songs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import client.Client;

public class SongList {
	public enum Song {
		SNOWMAN, LETITGO, LIFESTOOSHORT;
	}
	
	public static Map<Integer, List<String>> songs = new HashMap<Integer, List<String>>();
	
	public SongList() {
		for (Song val : Song.values()) {
			songs.put(new Integer(val.ordinal()), readFile("/res/songs/"+val.toString().toLowerCase()+".song"));
		}
	}
	
	public List<String> readFile(String pathInJar) {
		List<String> content = new ArrayList<String>();
		try (InputStream stream = getClass().getResourceAsStream(pathInJar)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		    if (stream!=null) {
		    	String str;
		        while ((str = reader.readLine()) != null) { 
		        	content.add(str);
		        }            
		    }        
		} catch (IOException e) {
			Client.getClient().reportException(e);
		}
		return content;
	}
}
