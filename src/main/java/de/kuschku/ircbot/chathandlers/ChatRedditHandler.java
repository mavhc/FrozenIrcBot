package de.kuschku.ircbot.chathandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.kuschku.ircbot.client.Client;
import de.kuschku.ircbot.client.MessageHandler;
import de.kuschku.ircbot.packets.MessagePacket;

public class ChatRedditHandler implements MessageHandler {
	public final static String trigger = "redditinfo";
		
	public static int subscribers;
	public static long lastSubscriberUpdate = 0;
	public static int getSubscriberDelta = 60000;

	@Override
	public boolean handleMessage(MessagePacket msg) {
		if (msg.message.equals("!redditinfo")) {
			int[] info;
			try {
				info = getSubscribers();
				Client.getClient().connection.send("PRIVMSG " + msg.channel + " :" + info[0] + " subscribed to /r/"+Client.configuration.get("subreddit"));
				Client.getClient().connection.send("PRIVMSG " + msg.channel + " :" + info[1] + " online on /r/"+Client.configuration.get("subreddit"));
			} catch (IOException e) {
			}
			return true;
		}
		return false;
	}
	
	private static int[] getSubscribers() throws IOException {
		lastSubscriberUpdate = System.currentTimeMillis();
		URL url = new URL("http://www.reddit.com/r/"+Client.configuration.get("subreddit")+"/");
        BufferedReader in = new BufferedReader(
        new InputStreamReader(url.openStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
           	if (inputLine.contains("<span class=\"subscribers\">")) {
        		int indexBegin = inputLine.indexOf("<span class=\"subscribers\">")+"<span class=\"subscribers\">".length();
        		int indexEnd = inputLine.substring(indexBegin).indexOf("</p>");
        		String line = inputLine.substring(indexBegin,indexBegin+indexEnd);
        		
                Pattern pattern = Pattern.compile("<span class='number'>(.*?)</span>");
                Matcher matcher = pattern.matcher(line);
                
            	int subscribers = -1;
            	int online = -1;
            	
            	if (matcher.find()) {
            		String number = matcher.group(1).replace(",", "");
            		subscribers = Integer.parseInt(number);
            	}
            	if (matcher.find()) {
            		String number = matcher.group(1).replace(",", "").replace("~", "");
            		online = Integer.parseInt(number);
            	}
            	if (subscribers != -1 || online != -1) {
            		return new int[]{subscribers,online};
            	}
        	}
        }
        in.close();
        return new int[]{-1,-1};
	}

}
