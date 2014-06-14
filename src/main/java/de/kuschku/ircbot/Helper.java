package de.kuschku.ircbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Helper {
	public static final ImmutableList<String> parseArgs(String command,
			String prefix) {
		if (command.contains(" ") && command.startsWith(prefix)) {
			return ImmutableList.copyOf((command.substring(prefix.length())+" ")
					.split(" "));
		} else {
			return null;
		}
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JsonObject readJsonFromUrl(String url)
			throws MalformedURLException, IOException {
		InputStream is = new URL(url).openStream();

		try (BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				Charset.forName("UTF-8")))) {
			String jsonText = readAll(rd);
			JsonObject json = new JsonParser().parse(jsonText)
					.getAsJsonObject();
			return json;
		}
	}
}