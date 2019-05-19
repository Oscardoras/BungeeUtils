package org.bungeeutils.io;

import org.bungeeutils.BungeePlugin;

public class Message {
	
	protected BungeePlugin plugin;
	protected String path;
	
	public Message(BungeePlugin plugin, String path) {
		this.plugin = plugin;
		this.path = path;
	}
	
	public BungeePlugin getPlugin() {
		return plugin;
	}
	
	public String getPath() {
		return path;
	}
	
}