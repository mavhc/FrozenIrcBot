package client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import net.Connection;
import net.NetConnection;
import net.TestConnection;
import packets.MessagePacket;
import packets.TextPacket;
import chathandlers.ChatRedditHandler;
import chathandlers.ControlHandler;
import chathandlers.ShipHandler;
import chathandlers.SongHandler;

public class Client {
	static Client client;
	
	public static Configuration configuration;

	public List<MessageHandler> handlers = new ArrayList<MessageHandler>();

	public Connection connection;

	public static void main(String[] args) {
		String configPath = "config.yml";
		Client.configuration = Configuration.fromFile(new File(configPath));
		
		client = new Client();
		try {
			client.start("irc.quakenet.org", 6667);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(String ip, int port) throws UnknownHostException,
			IOException {
		connection = (Boolean.parseBoolean(configuration.get("debug"))) ? new TestConnection() : new NetConnection();

		identify();

		connection.connect(ip, port);

		registerHandlers();

		connection.run();
	}

	/** Handles all incoming messages */
	public void respond(TextPacket textPacket) throws IOException {
		try {
			String packet = textPacket.text;
			Client.log("<<", packet);

			if (packet.contains("PING")) {
				connection.send("PONG " + packet.split(" ")[1]);
			} else if (packet.contains("|" + configuration.get("authName") + "|"
					+ " :Welcome to the QuakeNet IRC Network, " + "|" + configuration.get("authName") + "|")) {
				if (Boolean.parseBoolean(configuration.get("useAuth"))) {
					connection.send("PRIVMSG Q@CServe.quakenet.org :AUTH "
							+ configuration.get("authName") + " " + configuration.get("authPassword"));
				}
				connection.send("MODE " + "|" + configuration.get("authName") + "|" + " +x");
				connection.send("JOIN " + configuration.get("channel"));
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
		connection.send("NICK " + "|" + configuration.get("authName") + "|");
		connection.send("USER " + "|" + configuration.get("authName") + "|" + " 0 * :" + "|" + configuration.get("authName") + "|");
	}

	private void registerHandlers() {
		handlers.add(new ChatRedditHandler());
		handlers.add(new SongHandler());
		handlers.add(new ControlHandler());
		handlers.add(new ShipHandler());
	}

	public void reportException(Exception e) {
		connection.sendPrivate(configuration.get("owner"), "An error occured");
		connection.sendPrivate(configuration.get("owner"), e.getLocalizedMessage());

		Arrays.asList(e.getStackTrace()).forEach(
				x -> connection.sendPrivate(configuration.get("owner"), x.toString()));
	}

	public static boolean isUserOp(String user) {
		return user.trim().equalsIgnoreCase(configuration.get("owner"));
	}

	public static Client getClient() {
		return client;
	}

	public static void log(String side, String msg) {
		System.out.printf("[%s] %s\n", side, msg);
	}
}