package de.kuschku.ircbot;

import com.google.common.collect.ImmutableList;

public class ArgsParser {
	public static final ImmutableList<String> parseArgs(String command, String prefix) {
		if (command.contains(" ") && command.startsWith(prefix)) {
			return ImmutableList.copyOf(command.substring(prefix.length()).split(" "));
		} else {
			return null;
		}
	}
}
