package de.kuschku.ircbot.chathandlers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.tuple.Triple;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.kuschku.ircbot.client.Client;
import de.kuschku.ircbot.client.MessageHandler;
import de.kuschku.ircbot.packets.MessagePacket;

public class LinkTitleHandler implements MessageHandler {
	static final String regexOG = "<meta (.*)=(\"(og:title|title)\"|'(og:title|title)') content=(\"(.*)\"|'(.*)')(\\/>|>)";
	static final String regexGeneric = "<title>(.*)<\\/title>";
	static final String regexVideo = "<meta (.*)=(\"twitter:app:url:iphone\"|'twitter:app:url:iphone') content=(\"(.*)\"|'(.*)')(>|\\/>)";
	static final String regexRedirect = "<META http-equiv=\"refresh\" content=\"0;URL=(\"(.*)\"|'(.*)')>";

	static final Pattern patternOG = Pattern.compile(regexOG);
	static final Pattern patternGeneric = Pattern.compile(regexGeneric);
	static final Pattern patternVideo = Pattern.compile(regexVideo);
	static final Pattern patternRedirect = Pattern.compile(regexRedirect);

	public static enum Site {
		YOUTUBE, VIMEO, NONE
	}

	@Override
	public boolean handleMessage(MessagePacket msg) {
		String[] results = stringToURLList(msg.message);
		for (String result : results) {
			Triple<String, String, Site> value = extractTitleFromWebpage(result);
			if (value.getMiddle() != null && value.getRight() != Site.NONE) {
				try {
					Triple<String, Integer, Integer> data = getData(
							value.getMiddle(), value.getRight());
					DecimalFormat formatter = (DecimalFormat) NumberFormat
							.getInstance(Locale.US);

					String message = String.format("%s [%s|%s views]",
							((char) 2) + data.getLeft() + ((char) 2),
							nicetime(data.getMiddle().toString()),
							formatter.format(data.getRight()));
					Client.getClient().connection.send(msg.channel, message);
				} catch (FileNotFoundException e) {

				}
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
		InputStream is = null;
		int tries = 0;
		boolean redirect = true;
		try {
			String result = "";
			BufferedReader in = null;

			HttpURLConnection conn = null;
			while (redirect && tries < 20) {
				URL resourceUrl = new URL(address);
				HttpURLConnection.setFollowRedirects(true);
				conn = (HttpURLConnection) resourceUrl.openConnection();
				conn.setInstanceFollowRedirects(true);
				conn.setConnectTimeout(15000);
				conn.setReadTimeout(15000);
				conn.setRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 6.0; ru; rv:1.9.0.11) Gecko/2009060215 Firefox/3.0.11 (.NET CLR 3.5.30729)");
				conn.connect();

				redirect = false;

				// normally, 3xx is redirect
				int status = conn.getResponseCode();
				if (status != HttpURLConnection.HTTP_OK) {
					if (status == HttpURLConnection.HTTP_MOVED_TEMP
							|| status == HttpURLConnection.HTTP_MOVED_PERM
							|| status == HttpURLConnection.HTTP_SEE_OTHER) {
						redirect = true;
						address = conn.getHeaderField("Location");
					}
				} else {
					try {
						in = new BufferedReader(new InputStreamReader(
								is = conn.getInputStream()));
						String input = "";

						for (int i = 0; i < 5; i++) {
							if ((input = in.readLine())!=null)
								result += input + "\n";
						}

						if (result.contains("refresh")) {						
							int begin = result.indexOf("URL='");
							result = result.substring(begin + 5);
							result = result.substring(0, result.indexOf("'"));
							redirect = true;
							address = result;
						}
					} catch (IOException e) {
						System.out.println("error happened: " + e.toString());
					}
				}
			}
			String input;

			while ((input = in.readLine()) != null) {
				result += input + "\n";
			}

			return result;
		} catch (Exception e) {
			System.out.println("error happened: " + e.toString());
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return "";
	}

	private static Triple<String, Integer, Integer> getData(String value,
			Site site) throws FileNotFoundException {
		switch (site) {
		case YOUTUBE:
			JsonObject data = new JsonParser().parse(
					getPage("http://gdata.youtube.com/feeds/api/videos/"
							+ value + "?v=2&alt=jsonc")).getAsJsonObject();
			return Triple.of(
					StringEscapeUtils.unescapeHtml4(data.get("data")
							.getAsJsonObject().get("title").getAsString()),
					data.get("data").getAsJsonObject().get("duration")
							.getAsInt(), data.get("data").getAsJsonObject()
							.get("viewCount").getAsInt());
		case VIMEO:
			data = new JsonParser()
					.parse(getPage("http://vimeo.com/api/v2/video/" + value
							+ ".json")).getAsJsonArray().get(0)
					.getAsJsonObject();
			return Triple.of(StringEscapeUtils.unescapeHtml4(data.get("title")
					.getAsString()), data.get("duration").getAsInt(),
					data.get("stats_number_of_plays").getAsInt());
		default:
			throw new FileNotFoundException();
		}

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

	public static Triple<String, String, Site> extractTitleFromWebpage(
			String address) {
		Matcher matcher;

		String page = getPage(address);
		String title = "";
		String video_id = null;

		matcher = patternOG.matcher(page);
		if (matcher.find()) {
			title = matcher.group(6);
		}

		if (title == "") {
			matcher = patternGeneric.matcher(page);
			if (matcher.find()) {
				title = matcher.group(1);
			}
		}

		matcher = patternVideo.matcher(page);

		String presite = "NONE";
		while (matcher.find()) {
			video_id = matcher.group(4);

			presite = video_id.substring(0, video_id.indexOf(":/")).replace(
					"vnd.", "");
			video_id = video_id.substring(video_id.lastIndexOf("/") + 1);
		}

		Site site = Site.valueOf(presite.toUpperCase());

		return Triple.of(title, video_id, site);
	}
}
