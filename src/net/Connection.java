package net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

import packets.Packet;
import packets.TextPacket;
import client.Client;

public abstract class Connection {
	protected OutputStream out;
	protected InputStream in;

	Queue<TextPacket> output = new LinkedList<TextPacket>();
	
	public boolean isAuthed = false;

	ConnectionReader readThread;
	ConnectionWriter writeThread;
	public PingThread pingThread;

	public void run() {
		readThread = new ConnectionReader();
		writeThread = new ConnectionWriter();
		pingThread = new PingThread();

		readThread.start();
		writeThread.start();
		
		Client.getClient().identify();

		try {
			writeThread.join();
		} catch (InterruptedException e) {
		}

		readThread.quit();
	}

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
		Client.log(">>", text);

		TextPacket packet = new TextPacket();
		packet.text = text + "\r\n";
		synchronized (output) {
			output.add(packet);
		}
	}
	
	public void send(String channel, String text) {
		send("PRIVMSG " + channel + " :" + text);
	}

	public void sendPrivate(String user, String text) {
		send(String.format("PRIVMSG %s :%s", user, text));
	}

	public void quit() {
		send("QUIT Connection closed");
		writeThread.quit();
		pingThread.quit();
	}

	class ConnectionReader extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				try {
					int bytesAvailable = in.available();
					if (bytesAvailable > 0) {
						byte[] packetBytes = new byte[bytesAvailable];
						in.read(packetBytes, 0, packetBytes.length);
						TextPacket textPacket = new TextPacket();
						textPacket.parseFromInput(new DataInputStream(
								new ByteArrayInputStream(packetBytes)));
						Client.getClient().respond(textPacket);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
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
					synchronized (output) {
						while (output.size() > 0) {
							writePacket(output.remove());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public void quit() {
			this.run = false;
		}
	}
	
	public class PingThread extends Thread {

		public boolean run = true;

		@Override
		public void run() {
			while (this.run) {
				send("PING "+System.nanoTime());
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