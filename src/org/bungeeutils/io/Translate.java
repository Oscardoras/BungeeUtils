package org.bungeeutils.io;

import java.util.Map;
import java.util.Properties;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public final class Translate {
	private Translate() {}
	
	
	protected static String getMessage(String language, String path, Map<String, Properties> translates) {
		path = path.toLowerCase();
		if (translates.containsKey(language)) {
			Properties properties = translates.get(language);
			if (properties.containsKey(path)) return properties.getProperty(path);
		}
		if (translates.containsKey("en")) {
			Properties properties = translates.get("en");
			if (properties.containsKey(path)) return properties.getProperty(path);
		}
		for (String name : translates.keySet()) if (!name.equals(language) && !name.equals("en")) {
			Properties properties = translates.get(name);
			if (properties.containsKey(path)) return properties.getProperty(path);
		}
		throw new MessageException(path);
	}
	
	protected static String getPluginMessage(String language, Message message, String... args) {
		String msg = getMessage(language, message.getPath(), message.getPlugin().getTranslates());
		for (String arg : args) msg = msg.replaceFirst("%arg%", arg);
		return msg;
	}
	
	public static String getPluginMessage(CommandSender sender, Message message, String... args) {
		return getPluginMessage(getLanguage(sender), message, args);
	}
	
	protected static String getLanguage(CommandSender sender) {
		if (sender instanceof ProxiedPlayer) return ((ProxiedPlayer) sender).getLocale().getLanguage();
		else return "en";
	}
	
}
