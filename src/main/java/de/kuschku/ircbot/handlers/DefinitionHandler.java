package de.kuschku.ircbot.handlers;

import java.util.List;
import java.util.Optional;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import com.google.common.collect.ImmutableList;
import com.wordnik.client.api.WordApi;
import com.wordnik.client.common.ApiException;
import com.wordnik.client.model.Definition;

import de.kuschku.ircbot.ArgsParser;
import de.kuschku.ircbot.Client;
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

	public final static String formatDefinition(Definition definition) {
		return String.format(
				"%s %s %s",
				new BoldText(String.valueOf((Integer.valueOf(definition
						.getSequence()) + 1))),
				new ItalicText(definition.getPartOfSpeech()), definition
						.getText());
	}

	public final static Optional<List<Definition>> getDefinitions(String word,
			int amount) {
		word = URLParamEncoder.encode(word);
		try {
			WordApi api = new WordApi();
			api.getInvoker().addDefaultHeader("api_key",
					Client.fileConfiguration.get("dictionary_key"));
			List<Definition> definitions = api.getDefinitions(word, "all",
					"wordnet", amount, "false", "true", "false");
			return Optional.of(definitions);
		} catch (ApiException e) {
		}
		return null;
	}

	@Override
	public void onMessage(MessageEvent<PircBotX> event) throws Exception {
		ImmutableList<String> args = ArgsParser.parseArgs(event.getMessage(),
				"!");
		if (args != null && args.get(0).equalsIgnoreCase("define")) {
			int amount;
			if (event.getChannel().isChannelPrivate())
				amount = 20;
			else
				amount = 3;

			String word = String.join(" ", args.asList());
			word = word.substring(args.get(0).length() + 1);

			List<Definition> list = getDefinitions(word, amount).get();
			if (list.size() > 0) {
				for (Definition definition : list) {
					event.respond(formatDefinition(definition));
				}
				event.getChannel().send().message(String
						.format("More definitions at http://wordnetweb.princeton.edu/perl/webwn?s=%s",
								URLParamEncoder.encode(word)));
			} else {
				event.getChannel().send().message("Error: No definitions found for " + word);
			}
		}
	}

}
