package org.bungeeutils.io;

public class TranslatableMessageException extends IllegalArgumentException {
	
	private static final long serialVersionUID = 1592686378009645669L;
	
	public TranslatableMessageException(String path) {
		super(path);
	}
	
}