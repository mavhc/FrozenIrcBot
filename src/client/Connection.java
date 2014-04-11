package client;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import packets.Packet;
import packets.TextPacket;

public abstract class Connection {
	protected OutputStream out;
	InputStream in;
	LinkedList<Packet> packetQueue = new LinkedList<Packet>();

	public void writePacket(Packet packet) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataout = new DataOutputStream(baos);
		packet.writeToOutput(dataout);
		byte[] packetBytes = baos.toByteArray();
		out.write(packetBytes);
		out.flush();
	}

	abstract public void connect(String ip, int port);

	public void send(String text) {
		if (Client.DEBUG) Logger.getLogger(Client.name).log(Level.FINE, text);
		
		TextPacket packet = new TextPacket();
		packet.text = text + "\r\n";
		packetQueue.add(packet);
	}

	public void sendPrivate(String user, String text) {
		send(String.format("PRIVMSG %s :%s", user, text));
	}
	
	public void quit() {
		send("QUIT Connection closed");
	}
}