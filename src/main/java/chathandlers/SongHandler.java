package chathandlers;

import packets.MessagePacket;
import songs.SongList;
import client.Client;
import client.MessageHandler;

public class SongHandler implements MessageHandler {
	
	static int singAlongId = -1;
	static int singAlongLine = -1;
	static SongList songlist = new SongList();
	static int songLength = 0;

	@Override
	public boolean handleMessage(MessagePacket msg) {
		if (Client.isUserOp(msg.sender) && msg.message.contains("!singalong start")) {
			singAlongId = Integer.parseInt(msg.message.split("!singalong start")[1].trim());
			songLength = SongList.songs.get(singAlongId).size();
			singAlongLine = 1;
			Client.getClient().connection.send("PRIVMSG " + Client.configuration.get("channel") + " :" + SongList.songs.get(singAlongId).get(0));
			return true;
		} else if (Client.isUserOp(msg.sender) && msg.message.contains("!singalong stop")) {
			reset();
			return true;
		} else if (singAlongLine != -1) {
			if (equalsApproximate(singAlongLine,msg.message)) {
				singAlongLine++;
				Client.getClient().connection.send("PRIVMSG " + Client.configuration.get("channel") + " :" + SongList.songs.get(singAlongId).get(singAlongLine));
				singAlongLine++;
				
				if (singAlongLine == songLength-1) {
					// In case of odd length songs we send the last two lines at once.
					Client.getClient().connection.send("PRIVMSG " + Client.configuration.get("channel") + " :" + SongList.songs.get(singAlongId).get(singAlongLine));
					reset();
				} else if (singAlongLine == songLength) {
					reset();
				}
				return true;
			}
		}
		return false;
	}
	
	void reset() {
		songLength = -1;
		singAlongId = -1;
		singAlongLine = -1;
	}
	
	boolean equalsApproximate(int number, String string) {
		String originalLine = SongList.songs.get(singAlongId).get(number);
		String asciiUserInput = string.toLowerCase().replaceAll("[^a-zA-Z ]","");
		int distance = LevenshteinDistance(originalLine,asciiUserInput);
		return distance<5;
	}
	
	int minimum(int a, int b, int c) {
		return (a<=b) ? ((a<=c)? a : c) : ((b<=c)?b : c);
	}
	
	int LevenshteinDistance(String s, String t)
	{
	    // Code from wikipedia
	    if (s == t) return 0;
	    if (s.length() == 0) return t.length();
	    if (t.length() == 0) return s.length();
	 
	    int[] v0 = new int[t.length() + 1];
	    int[] v1 = new int[t.length() + 1];
	 
	    for (int i = 0; i < v0.length; i++)
	        v0[i] = i;
	 
	    for (int i = 0; i < s.length(); i++)
	    {
	        v1[0] = i + 1;
	 
	        for (int j = 0; j < t.length(); j++)
	        {
	            int cost = (s.codePointAt(i) == t.codePointAt(j)) ? 0 : 1;
	            v1[j + 1] = minimum(v1[j] + 1, v0[j + 1] + 1, v0[j] + cost);
	        }

	        for (int j = 0; j < v0.length; j++)
	            v0[j] = v1[j];
	    }
	 
	    return v1[t.length()];
	}

}
