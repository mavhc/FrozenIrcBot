package de.kuschku.ircbot.format;

public class UnderlinedText extends FormattedSpan {

	public UnderlinedText(String text) {
		super(text);
	}
	
	public UnderlinedText(FormattedSpan text) {
		super(text);
	}

	protected static final char PREFIX = (char) 0x1F;
	
	public String toString() {
		return PREFIX + text + PREFIX;
	}
}
