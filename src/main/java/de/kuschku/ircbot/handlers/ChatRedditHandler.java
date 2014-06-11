package de.kuschku.ircbot.handlers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import de.kuschku.ircbot.Client;

public class ChatRedditHandler extends ListenerAdapter<PircBotX> {
	public final static String trigger = "redditinfo";

	public static int subscribers;
	public static long lastSubscriberUpdate = 0;
	public static int getSubscriberDelta = 60000;

	@Override
	public void onMessage(MessageEvent<PircBotX> event) throws Exception {
		if (event.getMessage().equals("!redditinfo")) {
			String subreddit = Client.fileConfiguration.get(
					"subreddit");
			try {
				Pair<Integer, Integer> info = getSubscribers(subreddit);
				event.getChannel().send().message(String.format("%i subscribed to /r/%s",
						info.getLeft(), subreddit));
				event.getChannel().send().message(String.format("~%i online on /r/%s",
						info.getLeft(), subreddit));
			} catch (IOException e) {
				event.getChannel().send().message("Error: Can't get subscriber count");
			}
		}
	}

	private static Pair<Integer, Integer> getSubscribers(String subreddit)
			throws IOException, FileNotFoundException {
		lastSubscriberUpdate = System.currentTimeMillis();
		URL url = new URL("http://www.reddit.com/r/" + subreddit + "/");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.contains("<span class=\"subscribers\">")) {
				int indexBegin = inputLine
						.indexOf("<span class=\"subscribers\">")
						+ "<span class=\"subscribers\">".length();
				int indexEnd = inputLine.substring(indexBegin).indexOf("</p>");
				String line = inputLine.substring(indexBegin, indexBegin
						+ indexEnd);

				Pattern pattern = Pattern
						.compile("<span class='number'>(.*?)</span>");
				Matcher matcher = pattern.matcher(line);

				int subscribers = -1;
				int online = -1;

				if (matcher.find()) {
					subscribers = Integer.parseInt(matcher.group(1).replace(
							",", ""));
					if (matcher.find()) {
						online = Integer.parseInt(matcher.group(1)
								.replace(",", "").replace("~", ""));
					}
				}
				if (subscribers != -1 && online != -1) {
					return Pair.of(subscribers, online);
				}
			}
		}
		in.close();
		throw new FileNotFoundException();
	}
}
