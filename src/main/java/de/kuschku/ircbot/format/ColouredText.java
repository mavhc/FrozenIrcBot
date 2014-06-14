package de.kuschku.ircbot.format;

public class ColouredText extends FormattedSpan {
	
	public enum Color {
		
		BLACK(1),
		BLUE(2),
		BROWN(5),
		GREEN(3),
		GREY(14),
		LIGHT_BLUE(12),
		LIGHT_CYAN(11),
		LIGHT_GREEN(9),
		LIGHT_GREY(15),
		ORANGE(7),
		PINK(13),
		PURPLE(6),
		RED(4),
		TEAL(10),
		TRANSPARENT(99),
		WHITE(0),
		YELLOW(8);
		
		final int value;
		
		Color(int value) {
			this.value = value;
		}
	}
	protected static final char PREFIX = (char) 0x03;

	final Color background;

	final Color foreground;
	
	public ColouredText(String text, Color foreground, Color background) {
		super(text);
		this.foreground = foreground;
		this.background = background;
	}
	
	public ColouredText(FormattedSpan text, Color foreground, Color background) {
		super(text);
		this.foreground = foreground;
		this.background = background;
	}
	
	@Override
	public String toString() {
		return String.format("%s%02d,%02d%s%s",PREFIX,foreground.value,background.value,text,PREFIX);
	}
}
