package de.kuschku.ircbot.format;

public class BoldText extends FormattedSpan {

	public BoldText(String text) {
		super(text);
	}
	
	public BoldText(FormattedSpan text) {
		super(text);
	}

	protected static final char PREFIX = (char) 0x02;
	
	public String toString() {
		return PREFIX + text + PREFIX;
	}
}
