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
	
	public static void main(String[] args) {
		String test1 = ":blacklotus.ca.us.quakenet.org 353 ghjk = #Elsanna :ghjk neiromaru chu Keiko Joe Melancholia hkas_ Gilgamesh +Royalistic AndChat|8064 moving_Sir_hex Vogel`- TheStarkster indust @kuschku Lys|Toothbrush HKAS Toothbrushynn Sarinturn qnpe @Elsie KingGilgamesh ss7 @Q Elsabeth bean_dharma @|Pussy|";
		String test2 = ":blacklotus.ca.us.quakenet.org 366 ghjk #Elsanna :End of /NAMES list.";
		MessagePacket msg = MessagePacket.fromString(test1);
		System.out.println(msg.message);
	}

}
