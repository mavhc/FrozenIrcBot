package packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class TextPacket extends Packet {
	
	public String text;
	
	public TextPacket() {
		
	}
	
	public TextPacket(String text) {
		this.text = text;
	}
	
	public void writeToOutput(DataOutput out) throws IOException {
		out.writeBytes(text);
	}
	
	public void parseFromInput(DataInput in) throws IOException {
		text = in.readLine();
	}
}
