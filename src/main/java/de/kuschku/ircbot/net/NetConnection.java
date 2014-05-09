package de.kuschku.ircbot.net;

import java.io.IOException;
import java.net.Socket;

public class NetConnection extends Connection {
	public Socket socket;
	
	public void connect(String ip, int port) {
		try {
			socket = new Socket(ip, port);
			socket.setReuseAddress(true);
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
