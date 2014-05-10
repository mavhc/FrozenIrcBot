package de.kuschku.ircbot.client;

public class Logger {
	public static void reportException(Exception e) {
		Client.getClient().connection.sendPrivate(Client.configuration.get("owner"), "An error occured");
		Client.getClient().connection.sendPrivate(Client.configuration.get("owner"), e.getLocalizedMessage());
	
		for (StackTraceElement x : e.getStackTrace()) {
			Client.getClient().connection.sendPrivate(Client.configuration.get("owner"), x.toString());
		}
		
		e.printStackTrace();
	}
	
	public static void log(String side, String msg) {
		if (side!=">>"&&side!="<<"||Boolean.parseBoolean(Client.configuration.get("debug")))
			System.out.printf("[%s] [%s] %s\n", String.valueOf(System.currentTimeMillis()), side, msg.trim());
	}
}
