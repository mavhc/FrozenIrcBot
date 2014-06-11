package de.kuschku.ircbot.format;

public class BoldText extends FormattedSpan {

	char prefix = (char) 0x02;
	String text;
	
	public BoldText(String text) {
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
