package de.kuschku.ircbot.chathandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.kuschku.ircbot.client.Client;
import de.kuschku.ircbot.client.MessageHandler;
import de.kuschku.ircbot.packets.MessagePacket;

public class LinkTitleHandler implements MessageHandler {
	static final String regexOG = "<meta (.*)=(\"(og:title|title)\"|'(og:title|title)') content=(\"(.*)\"|'(.*)')(\\/>|>)";
	static final String regexGeneric = "<title>(.*)<\\/title>";
	static final String regexVideo = "<meta (.*)=(\"twitter:app:url:iphone\"|'twitter:app:url:iphone') content=(\"(.*)\"|'(.*)')(>|\\/>)";

	static Pattern patternOG = Pattern.compile(regexOG);
	static Pattern patternGeneric = Pattern.compile(regexGeneric);
	static Pattern patternVideo = Pattern.compile(regexVideo);
	
	@Override
	public boolean handleMessage(MessagePacket msg) {
		String[] results = stringToURLList(msg.message);
		for (String result : results) {
			Entry<String, String> value = extractTitleFromWebpage(result);
			if (value.getValue() != null) {
				JsonObject data = getData(value.getValue());
				DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
				
				String message = String.format("%s [%s|%s views]",
						((char)2)+StringEscapeUtils.unescapeHtml4(value.getKey())+((char)2),
						nicetime(data.get("data").getAsJsonObject().get("duration").getAsString()),
						formatter.format(data.get("data").getAsJsonObject().get("viewCount").getAsLong()));
				Client.getClient().connection.send(
						Client.configuration.get("channel"), message);
			}
		}
		return results.length > 0;
	}

	public static String nicetime(String time) {
		final long l = Long.valueOf(time) * 1000;

		final long hr = TimeUnit.MILLISECONDS.toHours(l);
		final long min = TimeUnit.MILLISECONDS.toMinutes(l
				- TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(l
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		if (l > 3600000) {
			return String.format("%02d:%02d:%02d", hr, min, sec);
		} else {
			return String.format("%02d:%02d", min, sec);
		}
	}

	public static String getPage(String address) {
		try {
			URL url = new URL(address);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()))) {
				String result = "";
				String input;

				while ((input = in.readLine()) != null) {
					result += input + "\n";
				}

				return result;
			} catch (IOException e) {
			}
		} catch (MalformedURLException e) {
		}
		return "";
	}

	private static JsonObject getData(String value) {
		return new JsonParser().parse(getPage("http://gdata.youtube.com/feeds/api/videos/"
				+ value + "?v=2&alt=jsonc")).getAsJsonObject();
	}

	public static String[] stringToURLList(String input) {
		List<String> results = new ArrayList<String>();

		Pattern pattern = Pattern
				.compile("(((http|https|spdy)\\:\\/\\/){1}\\S+)");
		Matcher matcher = pattern.matcher(input);
		while (matcher.find()) {
			results.add(matcher.group());
		}

		return results.toArray(new String[0]);
	}

	public static Entry<String, String> extractTitleFromWebpage(String address) {
		Matcher matcher;

		String page = getPage(address);
		String title = "";
		String video_id = null;
		
		matcher = patternOG.matcher(page);
		if (matcher.find()) {
			title = matcher.group(6);
		}
		
		if (title=="") {
			matcher = patternGeneric.matcher(page);
			if (matcher.find()) {
				title = matcher.group(1);
			}
		}
		
		matcher = patternVideo.matcher(page);
		while (matcher.find()) {
			video_id = matcher.group(4);
			video_id = video_id.substring(video_id.lastIndexOf("/") + 1);
		}
		
		return new AbstractMap.SimpleEntry<String, String>(title, video_id);
	}
}
