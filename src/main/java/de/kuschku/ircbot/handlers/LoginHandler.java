package de.kuschku.ircbot.handlers;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;

import de.kuschku.ircbot.Client;

public class LoginHandler extends ListenerAdapter<PircBotX> {
	@Override
	public void onConnect(ConnectEvent<PircBotX> event) throws Exception {
		if (Boolean.valueOf(Client.fileConfiguration.get("use_auth"))) {
			String authName = Client.fileConfiguration.get("auth_name");
			String authPassword = Client.fileConfiguration.get("auth_password");
			event.getBot().sendRaw().rawLine(String.format("AUTH %s %s",authName,authPassword));
			event.getBot().sendIRC().mode(event.getBot().getNick(), "+x");
		}
	}
}
