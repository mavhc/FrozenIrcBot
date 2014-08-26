package de.kuschku.ircbot.handlers;

import java.util.ArrayList;
import java.util.List;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import de.kuschku.ircbot.Client;
import de.kuschku.ircbot.Helper;

public class ControlHandler extends ListenerAdapter<PircBotX> {

    @Override
    public void onPrivateMessage(PrivateMessageEvent<PircBotX> event) throws Exception {

        if (event.getMessage().equals("!shutdown")) {
            if (event.getUser().getLogin().equals(Client.fileConfiguration.get("owner_login")) && event.getUser().getHostmask().equals(Client.fileConfiguration.get("owner_hostmask"))) {
	        System.err.println("Shutting down");
                System.exit(0);
            }
        }
    }
}
