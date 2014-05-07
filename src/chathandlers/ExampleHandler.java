package chathandlers;

import packets.MessagePacket;
import client.Client;
import client.MessageHandler;

public class ExampleHandler implements MessageHandler {

	@Override
	public boolean handleMessage(MessagePacket msg) {
		if (msg.message.indexOf(".marry")==0) {
			String[] args = (msg.message+" ").split(" ");
			
			
			Client.getClient().connection.send(Client.configuration.get("channel"), "You can't marry a man you just met");
			return true;
		}
		return false;
	}

}
