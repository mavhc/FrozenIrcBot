package client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.Connection;
import net.NetConnection;
import net.TestConnection;
import packets.MessagePacket;
import packets.TextPacket;

public class Client {
	static Client client;

	public static Configuration configuration;

	public List<MessageHandler> handlers = new ArrayList<MessageHandler>();

	public Connection connection;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T newInstance(final String className,
			final Object... args) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		// Derive the parameter types from the parameters themselves.
		Class[] types = new Class[args.length];
		for (int i = 0; i < types.length; i++) {
			types[i] = args[i].getClass();
		}
		return (T) Class.forName(className).getConstructor(types)
				.newInstance(args);
	}

	public static void main(String[] args) {
		String configPath = "config.yml";
		for (String arg : args) {
			if (arg.contains("--config="))
				configPath = arg.substring(arg.indexOf("--config=")
						+ "--config=".length());
		}
		Client.configuration = Configuration.fromFile(new File(configPath));

		client = new Client();
		try {
			client.start("irc.quakenet.org", 6667);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void restart(long timeout) {
		getClient().connection.quit();
		
		try {
			getClient().wait(timeout);
		} catch (InterruptedException e1) {
		}
		
		client = new Client();
		try {
			client.start("irc.quakenet.org", 6667);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start(String ip, int port) throws UnknownHostException,
			IOException {
		connection = (Boolean.parseBoolean(configuration.get("debug"))) ? new TestConnection()
				: new NetConnection();

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
				if (!connection.isAuthed) {
					connection.send("IDENT " + "|" + configuration.get("authName") + "|");
					connection.send("USER " + "|" + configuration.get("authName") + "|"
							+ " 0 * :" + "|" + configuration.get("authName") + "|");
					connection.isAuthed = true;
				}
			} else if (packet.contains("|" + configuration.get("authName")
					+ "|" + " :Welcome to the QuakeNet IRC Network, " + "|"
					+ configuration.get("authName") + "|")) {
				if (Boolean.parseBoolean(configuration.get("useAuth"))) {
					connection.send("PRIVMSG Q@CServe.quakenet.org :AUTH "
							+ configuration.get("authName") + " "
							+ configuration.get("authPassword"));
				}
				connection.send("MODE " + "|" + configuration.get("authName")
						+ "|" + " +x");
				connection.send("JOIN " + configuration.get("channel"));
				connection.pingThread.start();
			} else if (packet.startsWith("ERROR")) {
				restart(2000);
			} else if (packet.startsWith(":") && packet.contains("PRIVMSG")) {
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

	public void identify() {
		connection.send("PASS " + UUID.randomUUID());
		connection.send("NICK " + "|" + configuration.get("authName") + "|");
	}

	private void registerHandlers() {
		for (String handler : configuration.getHandlers()) {
			try {
				handlers.add((MessageHandler) newInstance(handler));
			} catch (ClassNotFoundException | NoSuchMethodException
					| InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	public void reportException(Exception e) {
		connection.sendPrivate(configuration.get("owner"), "An error occured");
		connection.sendPrivate(configuration.get("owner"),
				e.getLocalizedMessage());

		for (StackTraceElement x : e.getStackTrace()) {
			connection.sendPrivate(configuration.get("owner"), x.toString());
		}
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