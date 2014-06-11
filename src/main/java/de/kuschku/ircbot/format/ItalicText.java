package de.kuschku.ircbot.format;

public class ItalicText extends FormattedSpan {

	char prefix = (char) 0x1D;
	String text;

	public ItalicText(String text) {
		this.text = text;
	}

	@Override
	public String getRawText() {
		return this.text;
	}

	@Override
	public String toString() {
		return prefix + text + prefix;
	}
}
