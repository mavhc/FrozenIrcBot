package de.kuschku.ircbot.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class Packet {
	
	public void writeToOutput(DataOutput out) throws IOException {
		System.err.println("Default writeToOutput");
	}
	
	public void parseFromInput(DataInput in) throws IOException {
		System.err.println("Default parseFromInput");
	}
	
	public abstract String toString();
}
