package org.bungeeutils.io;

public class MessageException extends IllegalArgumentException {
	
	private static final long serialVersionUID = 1592686378009645669L;
	
	public MessageException(String path) {
		super(path);
	}
	
}