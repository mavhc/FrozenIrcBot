package de.kuschku.ircbot.client;

public class Logger {
	public static void reportException(Exception e) {
		Client.getClient().connection.sendPrivate(Client.configuration.get("owner"), "An error occured");
		Client.getClient().connection.sendPrivate(Client.configuration.get("owner"), e.getLocalizedMessage());
	
		for (StackTraceElement x : e.getStackTrace()) {
			Client.getClient().connection.sendPrivate(Client.configuration.get("owner"), x.toString());
		}
	}
	
	public static void log(String side, String msg) {
		System.out.printf("[%s] [%s] %s\n", String.valueOf(System.currentTimeMillis()), side, msg.trim());
	}
}
