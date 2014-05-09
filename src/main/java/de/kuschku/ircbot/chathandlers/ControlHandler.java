package de.kuschku.ircbot.chathandlers;

import de.kuschku.ircbot.client.Client;
import de.kuschku.ircbot.client.MessageHandler;
import de.kuschku.ircbot.packets.MessagePacket;

public class ControlHandler implements MessageHandler {
	@Override
	public boolean handleMessage(MessagePacket msg) {
		if (Client.isUserOp(msg.sender)) {
			switch (msg.message) {
			case "!stop":
				Client.getClient().connection.quit();
				return true;
			}
		}
		return false;
	}
}
