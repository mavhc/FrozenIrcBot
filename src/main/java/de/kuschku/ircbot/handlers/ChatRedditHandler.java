package de.kuschku.ircbot.handlers;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.lang3.tuple.Pair;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.google.gson.JsonObject;

import de.kuschku.ircbot.Client;
import de.kuschku.ircbot.Helper;
import de.kuschku.ircbot.format.BoldText;

public class ChatRedditHandler extends ListenerAdapter<PircBotX> {

	@Override
	public void onMessage(MessageEvent<PircBotX> event) throws Exception {
		if (event.getMessage().equals("!redditinfo")) {
			String subreddit = Client.fileConfiguration.get(
					"subreddit");
			
			try {
				Pair<Integer, Integer> info = getSubscribers(subreddit);
				event.getChannel().send().message(String.format("%s subscribed to /r/%s",
						info.getLeft().toString(), subreddit));
				event.getChannel().send().message(String.format("~%s online on /r/%s",
						info.getRight().toString(), subreddit));
			} catch (IOException e) {
				event.getChannel().send().message(new BoldText("Error: Couldn't get subscriber count").toString());
			}
		}
	}

	private static Pair<Integer, Integer> getSubscribers(String subreddit)
			throws MalformedURLException, IOException {
		JsonObject data = Helper.readJsonFromUrl(String.format(
				"http://www.reddit.com/r/%s/about.json", subreddit)).get("data").getAsJsonObject();
		return Pair.of(data.get("subscribers").getAsInt(),data.get("accounts_active").getAsInt());
	}
}
