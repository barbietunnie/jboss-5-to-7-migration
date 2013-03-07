package jpa.util;

public class EmailSenderException extends Exception {
	private static final long serialVersionUID = 1675797697586494571L;

	public EmailSenderException(String message) {
		super(message);
	}
	
	public EmailSenderException(String message, Throwable cause) {
		super(message, cause);
	}
}
