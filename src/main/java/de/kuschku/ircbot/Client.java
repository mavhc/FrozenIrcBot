package de.kuschku.ircbot;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.Listener;

public class Client {
	
	public static FileConfiguration fileConfiguration;
	private PircBotX bot;

	public static void main(String[] args) {
		Options options = new Options();
		CmdLineParser parser = new CmdLineParser(options);
		try {
			parser.parseArgument(args);
			new Client(options);
		} catch (CmdLineException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public Client(Options options) {
		try {
			fileConfiguration = FileConfiguration.fromFile(new File(options.configpath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
		// Setup this bot
		Configuration.Builder<PircBotX> builder = new Configuration.Builder<PircBotX>()
				.setName(fileConfiguration.get("name"))
				.setLogin(fileConfiguration.get("auth_name"))
				.setAutoNickChange(false).setCapEnabled(true)
				.setServerHostname(fileConfiguration.get("hostname"))
				.addAutoJoinChannel(fileConfiguration.get("channel"));

		for (String handler : fileConfiguration.getHandlers()) {
			try {
				builder.addListener((Listener<PircBotX>) newInstance(handler));
			} catch (ClassNotFoundException | NoSuchMethodException
					| InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				System.err
						.println("Handler could not be activated: " + handler);
				e.printStackTrace();
			}
		}

		this.bot = new PircBotX(builder.buildConfiguration());

		try {
			this.bot.startBot();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static class Options {
		@Option(name = "-config")
		private String configpath = "config.yml";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T newInstance(final String className,
			final Object... args) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		// Derive the parameter types from the parameters themselves.
		Class[] types = new Class[args.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = args[i].getClass();
		}
		return (T) Class.forName(className).getConstructor(types)
				.newInstance(args);
	}
}
