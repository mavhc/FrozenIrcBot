package net;

public class TestConnection extends Connection {

	@Override
	public void connect(String ip, int port) {
		out = System.out;
		in = System.in;
	}

}
