package client;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.Connection;
import net.NetConnection;
import net.PermLevel;
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

	public void restart(final long timeout) {
		connection.quit();

		Thread th = new Thread(new Thread() {
			@Override
			public void run() {
				try {
					sleep(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		th.start();
		try {
			th.join();
			start("irc.quakenet.org", 6667);
		} catch (InterruptedException | IOException e) {
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
			Logger.log("<<", packet);

			if (packet.contains("PING")) {
				connection.send("PONG " + packet.split(" ")[1]);
				if (!connection.isAuthed) {
					connection.send("IDENT " + "|"
							+ configuration.get("authName") + "|");
					connection.send("USER " + "|"
							+ configuration.get("authName") + "|" + " 0 * :"
							+ "|" + configuration.get("authName") + "|");
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
				connection.quit();
			} else if (packet.startsWith(":")) {
				MessagePacket msg = MessagePacket.fromString(packet);
				
				if (msg.command.equalsIgnoreCase("PRIVMSG")) {
				for (MessageHandler handler : handlers) {
					if (handler.handleMessage(msg)) {
						break;
					}
				}
				} else if (msg.command.equalsIgnoreCase("353")) {
					String[] users = msg.message.split(" ");
					Map<String,PermLevel> userlist = new HashMap<String,PermLevel>();
					
					for (String user : users) {
						if (user.startsWith("+")) {
							userlist.put(user.substring(1), PermLevel.VOICE);
						} else if (user.startsWith("@")) {
							userlist.put(user.substring(1), PermLevel.OPERATOR);
						} else {
							userlist.put(user, PermLevel.USER);
						}
					}
					
					connection.users = userlist;
				}
			}
		} catch (Exception e) {
			Logger.reportException(e);
		}
	}

	private void registerHandlers() {
		for (String handler : configuration.getHandlers()) {
			try {
				handlers.add((MessageHandler) newInstance(handler));
			} catch (ClassNotFoundException | NoSuchMethodException
					| InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				System.err.println("Handler could not be activated: "+handler);
				e.printStackTrace();
			}
		}
	}

	public static boolean isUserOp(String user) {
		return getClient().connection.users.get(user)==PermLevel.OPERATOR;
	}

	public static Client getClient() {
		return client;
	}
}