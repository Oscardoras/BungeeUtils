package org.bungeeutils.io;

/** Represents a translatable message not found. */
public class TranslatableMessageException extends IllegalArgumentException {
	
	private static final long serialVersionUID = 1592686378009645669L;
	
	/**
	 * Represents a translatable message not found.
	 * @param path the path of the translatable message in translation files which was not found
	 */
	public TranslatableMessageException(String path) {
		super(path);
	}
	
}