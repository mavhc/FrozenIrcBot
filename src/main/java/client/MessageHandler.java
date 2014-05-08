package client;

import packets.MessagePacket;

public interface MessageHandler {
	public boolean handleMessage(MessagePacket msg);
}
