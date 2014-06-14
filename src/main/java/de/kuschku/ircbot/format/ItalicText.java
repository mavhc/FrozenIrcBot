package de.kuschku.ircbot.format;

public class ItalicText extends FormattedSpan {

	public ItalicText(String text) {
		super(text);
	}
	
	public ItalicText(FormattedSpan text) {
		super(text);
	}

	protected static final char PREFIX = (char) 0x1D;
	
	public String toString() {
		return PREFIX + text + PREFIX;
	}
}
