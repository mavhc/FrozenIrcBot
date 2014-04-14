package chathandlers;

import packets.MessagePacket;
import client.Client;
import client.MessageHandler;

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
