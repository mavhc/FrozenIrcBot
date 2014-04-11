package client;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import packets.MessagePacket;
import packets.TextPacket;
import chathandlers.ChatRedditHandler;
import chathandlers.ControlHandler;
import chathandlers.SongHandler;

public class Client {
	
	private final static String authName = "kDebugBot";
	private final static String authPassword = "";
	
	private final static String owner = "Annelsa";
	
	/** Defines if the bot should authenticate itself on IRC */
	private boolean useAuthentication = true;
	
	public static final boolean DEBUG = false;
	
	public final static String name = "|"+authName+"|";
	public final static String channel = "#frozen";
	
	static Client client;
	
	public List<MessageHandler> handlers = new ArrayList<MessageHandler>();
	
	public boolean run = false;
	
	public Connection connection;
	
	public static void main(String[] args) {
		client = new Client();
		try {
			client.start("irc.quakenet.org", 6667);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start(String ip, int port) throws UnknownHostException, IOException {
		connection = (DEBUG) ? new TestConnection() : new NetConnection();
		run = true;
		
		Logger.getLogger(name).log(Level.INFO, "Connecting");
		
		identify();
		
		connection.connect(ip,port);
		
		new Thread() {
			@Override
			public void run() {
				while(run) {
					try {
						int bytesAvailable = connection.in.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							connection.in.read(packetBytes, 0, packetBytes.length);
							TextPacket textPacket = new TextPacket();
							textPacket.parseFromInput(new DataInputStream(new ByteArrayInputStream(packetBytes)));
							respond(textPacket);
						}
						Thread.sleep(50);
					} catch (Exception e) {
						run = false;
					}
				}
			}
		}.start();
		
		new Thread() {
			@Override
			public void run() {
				while(run) {
					try {
						if (connection.packetQueue.size() > 0) {
							connection.writePacket(connection.packetQueue.remove());
						}
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
						run = false;
					}
				}
			}
		}.start();
		
		registerHandlers();
	}
	
	/** Handles all incoming messages */	
	private void respond(TextPacket textPacket) throws IOException {
		try {
			String t = textPacket.text;
			if (t.contains("PING")) {
				Logger.getLogger(name).log(Level.INFO, "Returning Ping");
				connection.send("PONG " + t.split(" ")[1]);
			} else if (t.contains(name + " :Welcome to the QuakeNet IRC Network, "+name)) {
				if (useAuthentication) {
					Logger.getLogger(name).log(Level.INFO, "Authentication in progress");
					connection.send("PRIVMSG Q@CServe.quakenet.org :AUTH " + authName + " " + authPassword);
				}
				Logger.getLogger(name).log(Level.INFO, String.format("Joining %s",channel));
				connection.send("MODE " + name + " +x");
				connection.send("JOIN " + channel);
			} else if (t.startsWith(":")) {
				MessagePacket msg = MessagePacket.fromString(t);
					
				for (MessageHandler handler : handlers) {
					if (handler.handleMessage(msg)) {
						Logger.getLogger(name).log(Level.INFO, String.format("COMMAND: %s",t));
						break;
					}
				}
			}
		} catch(Exception e) {
			reportException(e);
		}
	}
	
	private void identify() {
		connection.send("PASS " + UUID.randomUUID());
		connection.send("NICK " + name);
		connection.send("USER FrozenBot 0 * :Frozen Bot");
	}

	private void registerHandlers() {
		handlers.add(new ChatRedditHandler());
		handlers.add(new SongHandler());
		handlers.add(new ControlHandler());
	}
	
	public void reportException(Exception e) {
		connection.sendPrivate(owner, "An error occured");
		connection.sendPrivate(owner, e.getLocalizedMessage());
		for (StackTraceElement sttrel : e.getStackTrace()) {
			connection.sendPrivate(owner, sttrel.toString());
		}
		Logger.getLogger(name).log(Level.INFO, e.getMessage());
	}
	
	public static boolean isUserOp(String user) {
		return user.trim().equalsIgnoreCase(owner);
	}
	
	public static Client getClient() {
		return client;
	}
}