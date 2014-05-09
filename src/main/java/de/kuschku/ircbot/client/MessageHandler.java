package de.kuschku.ircbot.client;

import de.kuschku.ircbot.packets.MessagePacket;

public interface MessageHandler {
	public boolean handleMessage(MessagePacket msg);
}
