package de.kuschku.ircbot.format;

public abstract class FormattedSpan {
	
	protected final static char PREFIX = 0;
	protected final String text;
	
	FormattedSpan(String text) {
		this.text = text;
	}
	
	FormattedSpan(FormattedSpan text) {
		this.text = text.toString();
	}
}
