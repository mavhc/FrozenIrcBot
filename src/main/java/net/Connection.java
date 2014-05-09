package net;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import packets.Packet;
import packets.TextPacket;
import client.Client;
import client.Logger;

public abstract class Connection {
	protected OutputStream out;
	protected InputStream in;

	Queue<TextPacket> output = new LinkedList<TextPacket>();
	
	public boolean isAuthed = false;
	
	public Map<String,PermLevel> users = new HashMap<String,PermLevel>();

	ConnectionReader readThread;
	ConnectionWriter writeThread;
	public PingThread pingThread;

	public void run() {
		readThread = new ConnectionReader();
		writeThread = new ConnectionWriter();
		pingThread = new PingThread();

		readThread.start();
		writeThread.start();
		
		send("PASS " + UUID.randomUUID());
		send("NICK " + "|" + Client.configuration.get("authName") + "|");

		try {
			writeThread.join();
		} catch (InterruptedException e) {
		}

		readThread.quit();
	}

	public void writePacket(Packet packet) throws IOException {
		Logger.log(">>", packet.toString());
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dataout = new DataOutputStream(baos);
		packet.writeToOutput(dataout);
		byte[] packetBytes = baos.toByteArray();
		out.write(packetBytes);
		out.flush();
	}

	abstract public void connect(String ip, int port);

	public void send(String text) {
		TextPacket packet = new TextPacket();
		packet.text = text + "\r\n";
		output.add(packet);
	}
	
	public void send(String channel, String text) {
		send("PRIVMSG " + channel + " :" + text);
	}

	public void sendPrivate(String user, String text) {
		send(String.format("PRIVMSG %s :%s", user, text));
	}

	public void quit() {
		writeThread.quit();
		pingThread.quit();
	}

	class ConnectionReader extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				try {
					while((line = reader.readLine()) != null) {
						try {
							Client.getClient().respond(new TextPacket(line));
						} catch (IOException e) {
							Logger.reportException(e);
						}
					}
					sleep(100);
				} catch (IOException | InterruptedException e) {
					Logger.reportException(e);
				}
			}
		}

		public void quit() {
			this.run = false;
		}
	}

	class ConnectionWriter extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				try {
					while (output.size() > 0) {
						writePacket(output.remove());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void quit() {
			TextPacket packet = new TextPacket();
			packet.text = "QUIT :Connection Closed by User" + "\r\n";
			
			try {
				writePacket(packet);
			} catch (IOException e) {
			}
			this.run = false;
		}
	}
	
	public class PingThread extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				send("PING "+System.nanoTime());
				send("NAMES "+Client.configuration.get("channel"));
				try {
					sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void quit() {
			this.run = false;
		}
	}
}