package de.kuschku.ircbot.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.wordnik.client.api.WordApi;
import com.wordnik.client.common.ApiException;
import com.wordnik.client.model.Definition;

import de.kuschku.ircbot.Client;
import de.kuschku.ircbot.Helper;
import de.kuschku.ircbot.format.BoldText;
import de.kuschku.ircbot.format.ItalicText;

public class DefinitionHandler extends ListenerAdapter<PircBotX> {

	public static class URLParamEncoder {

		public static String encode(String input) {
			StringBuilder resultStr = new StringBuilder();
			for (char ch : input.toCharArray()) {
				if (isUnsafe(ch)) {
					resultStr.append('%');
					resultStr.append(toHex(ch / 16));
					resultStr.append(toHex(ch % 16));
				} else {
					resultStr.append(ch);
				}
			}
			return resultStr.toString();
		}

		private static boolean isUnsafe(char ch) {
			if (ch > 128 || ch < 0)
				return true;
			return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
		}

		private static char toHex(int ch) {
			return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
		}

	}

	@Override
	public void onMessage(MessageEvent<PircBotX> event) throws Exception {
		ImmutableList<String> args = Helper.parseArgs(event.getMessage(), "!");
		if (args != null && (args.get(0).equalsIgnoreCase("define") || args.get(0).equalsIgnoreCase("urban"))) {
			int amount = 3;
			if (event.getChannel().isChannelPrivate())
				amount = 20;

			String word = String.join(" ", args.asList());
			word = word.substring(args.get(0).length() + 1);
			
			Backend backend = null;
			
			if (args.get(0).equalsIgnoreCase("define")) {
				backend = new WordNetBackend(Client.fileConfiguration.get("wordnet_key"));
			} else if (args.get(0).equalsIgnoreCase("urban")) {
				backend = new UrbanBackend(Client.fileConfiguration.get("urban_key"));
			}
			

			List<String> list = backend.getDefinition(word, amount);

			if (list.size() > 0) {
				list.forEach(msg -> event.getChannel().send().message(msg));

				event.getChannel().send().message(backend.getFooter(word));
			} else {
				event.getChannel()
						.send()
						.message(
								new BoldText(String.format(
										"Error: no definitions found for %s",
										word)).toString());
			}
		}
	}

	interface Backend {
		public List<String> getDefinition(String word, int amount);

		public String getFooter(String word);
	}

	class WordNetBackend implements Backend {

		final String key;

		WordNetBackend(String key) {
			this.key = key;
		}

		public Optional<List<Definition>> getDefinitions(String word, int amount) {
			word = URLParamEncoder.encode(word);
			try {
				WordApi api = new WordApi();
				api.getInvoker().addDefaultHeader("api_key", key);
				List<Definition> definitions = api.getDefinitions(word, "all",
						"wordnet", amount, "false", "true", "false");
				return Optional.of(definitions);
			} catch (ApiException e) {
			}
			return Optional.ofNullable(null);
		}

		@Override
		public List<String> getDefinition(String word, int amount) {
			List<Definition> list = getDefinitions(word, amount).get();
			List<String> result = new ArrayList<String>();
			for (Definition definition : list) {
				result.add(formatDefinition(definition));
			}
			return result;
		}

		public String formatDefinition(Definition definition) {
			return String.format(
					"%s %s %s",
					new BoldText(String.valueOf((Integer.valueOf(definition
							.getSequence()) + 1))),
					new ItalicText(definition.getPartOfSpeech()), definition
							.getText());
		}

		@Override
		public String getFooter(String word) {
			return String
					.format("More definitions at http://wordnetweb.princeton.edu/perl/webwn?s=%s",
							URLParamEncoder.encode(word));
		}
	}
	
	class UrbanBackend implements Backend {

		final String key;

		UrbanBackend(String key) {
			this.key = key;
		}

		@Override
		public List<String> getDefinition(String word, int amount) {
			word = URLParamEncoder.encode(word);
			try {
				JsonObject result = Helper.readJsonFromUrl(String.format("http://api.urbandictionary.com/v0/define?term=%s&key=%s",word,key));
				JsonArray definitions = result.get("list").getAsJsonArray();
				List<String> results = new ArrayList<String>();
				for (int i = 0; i < amount && i < definitions.size(); i++) {
					results.add(String.format("%s %s",new BoldText(String.valueOf(i)),definitions.get(i).getAsJsonObject().get("definition").getAsString()));
				}
				return results;
			} catch (IOException e) {
			}
			return new ArrayList<String>();
		}

		@Override
		public String getFooter(String word) {
			return String
					.format("More definitions at http://www.urbandictionary.com/define.php?term=%s",
							URLParamEncoder.encode(word));
		}
	}

}
