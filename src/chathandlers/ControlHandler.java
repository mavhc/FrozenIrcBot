package chathandlers;

import java.util.logging.Logger;

import packets.MessagePacket;
import client.Client;
import client.MessageHandler;

public class ControlHandler implements MessageHandler {
	@Override
	public boolean handleMessage(MessagePacket msg) {
		if (Client.isUserOp(msg.sender)) {
			System.out.println(msg.message);
			switch (msg.message) {
			case "!stop":
				Logger.getLogger(Client.name).info("Closing");
				Client.getClient().connection.quit();
				return true;
			}
		}
		return false;
	}
}
