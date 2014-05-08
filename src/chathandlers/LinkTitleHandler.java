package chathandlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;

import com.json.parsers.JSONParser;
import com.json.parsers.JsonParserFactory;

import packets.MessagePacket;
import client.Client;
import client.MessageHandler;

public class LinkTitleHandler implements MessageHandler {
	@Override
	public boolean handleMessage(MessagePacket msg) {
		String[] results = stringToURLList(msg.message);
		for (String result : results) {
			Entry<String, String> value = extractTitleFromWebpage(result);
			if (value.getValue() != null) {
				Map data = getData(value.getValue());
				String message = String.format("%s [%s|%s views]",
						StringEscapeUtils.unescapeHtml4(value.getKey()),
						nicetime((String) ((Map) data.get("data"))
								.get("duration")), ((Map) data.get("data"))
								.get("viewCount"));
				Client.getClient().connection.send(
						Client.configuration.get("channel"), message);
			}
		}
		return results.length > 0;
	}

	public static void main(String[] args) {
		Entry t = extractTitleFromWebpage("https://www.youtube.com/watch?v=BS0T8Cd4UhA");
		System.out.println(t.getKey() + ":" + t.getValue());
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

	private static Map getData(String value) {
		JsonParserFactory factory = JsonParserFactory.getInstance();
		JSONParser parser = factory.newJsonParser();

		return parser
				.parseJson(getPage("http://gdata.youtube.com/feeds/api/videos/"
						+ value + "?v=2&alt=jsonc"));
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

		final String regex_og = "<meta (.*)=(\\\"(og:title|title)\\\"|\\'(og:title|title)\\') content=(\\\"(.*)\\\"|\\'(.*)\\')(\\/>|>)";
		final String regex_generic = "<title>(.*)<\\/title>";
		final String regex_video = "<meta (.*)=(\\\"twitter:app:url:iphone\\\"|\\\'twitter:app:url:iphone\\\') (content=\\\"(.*)\\\"|content=\\\'(.*)\\\')(\\/?)>";

		Pattern pattern_og = Pattern.compile(regex_og);
		Pattern pattern_generic = Pattern.compile(regex_generic);
		Pattern pattern_video = Pattern.compile(regex_video);

		Matcher matcher;
		
		System.out.println(pattern_video.toString());

		String page = getPage(address);
		String title = "null";
		String video_id = null;
		
		matcher = pattern_og.matcher(page);
		if (matcher.find()) {
			title = matcher.group(6);
		}
		
		matcher = pattern_generic.matcher(page);
		if (matcher.find()) {
			page = matcher.group(1);
		}
		
		matcher = pattern_video.matcher(page);
		if (matcher.find()) {
			System.out.println("found something");
			video_id = matcher.group();
			//video_id = video_id.substring(video_id.lastIndexOf("/") + 1);
		}
		
		return new AbstractMap.SimpleEntry<String, String>(title, video_id);
	}
}
