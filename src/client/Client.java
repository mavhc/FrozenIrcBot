package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import packets.MessagePacket;
import packets.TextPacket;
import chathandlers.ChatRedditHandler;
import chathandlers.ControlHandler;
import chathandlers.SongHandler;

public class Client {

	private final static String authName = "kBotDebug";
	private final static String authPassword = "";

	private final static String owner = "kuschku";

	/** Defines if the bot should authenticate itself on IRC */
	private boolean useAuthentication = false;

	public static final boolean DEBUG = false;

	public final static String name = "|" + authName + "|";
	public final static String channel = "#frozen";

	static Client client;

	public List<MessageHandler> handlers = new ArrayList<MessageHandler>();

	public Connection connection;

	public static void main(String[] args) {
		client = new Client();
		try {
			client.start("irc.quakenet.org", 6667);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(String ip, int port) throws UnknownHostException,
			IOException {
		connection = (DEBUG) ? new TestConnection() : new NetConnection();

		identify();

		connection.connect(ip, port);

		registerHandlers();

		connection.run();
	}

	/** Handles all incoming messages */
	void respond(TextPacket textPacket) throws IOException {
		try {
			String packet = textPacket.text;
			Client.log("<<", packet);

			if (packet.contains("PING")) {
				connection.send("PONG " + packet.split(" ")[1]);
			} else if (packet.contains(name
					+ " :Welcome to the QuakeNet IRC Network, " + name)) {
				if (useAuthentication) {
					connection.send("PRIVMSG Q@CServe.quakenet.org :AUTH "
							+ authName + " " + authPassword);
				}
				connection.send("MODE " + name + " +x");
				connection.send("JOIN " + channel);
			} else if (packet.startsWith(":")) {
				MessagePacket msg = MessagePacket.fromString(packet);

				for (MessageHandler handler : handlers) {
					if (handler.handleMessage(msg)) {
						break;
					}
				}
			}
		} catch (Exception e) {
			reportException(e);
		}
	}

	private void identify() {
		connection.send("PASS " + UUID.randomUUID());
		connection.send("NICK " + name);
		connection.send("USER " + name + " 0 * :" + name);
	}

	private void registerHandlers() {
		handlers.add(new ChatRedditHandler());
		handlers.add(new SongHandler());
		handlers.add(new ControlHandler());
	}

	public void reportException(Exception e) {
		connection.sendPrivate(owner, "An error occured");
		connection.sendPrivate(owner, e.getLocalizedMessage());

		Arrays.asList(e.getStackTrace()).forEach(
				x -> connection.sendPrivate(owner, x.toString()));
	}

	public static boolean isUserOp(String user) {
		return user.trim().equalsIgnoreCase(owner);
	}

	public static Client getClient() {
		return client;
	}

	public static void log(String side, String msg) {
		System.out.printf("[%s] %s\n", side, msg);
	}
}