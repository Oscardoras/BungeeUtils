package org.bungeeutils.io;

import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

/** Sends messages */
public final class SendMessage {
	private SendMessage() {}
	
	
	/**
	 * Sends a message
	 * @param sender the sender to send the message
	 * @param message the message to send
	 */
	public static void send(CommandSender sender, String message) {
		sender.sendMessage(new TextComponent(ChatColor.RESET + message));
	}
	
	/**
	 * Sends a message to the console
	 * @param message the message to send
	 */
	public static void out(String message) {
		send(ProxyServer.getInstance().getConsole(), message);
	}
	
	/**
	 * Sends a list string
	 * @param sender the sender to send the message
	 * @param list the list to send
	 */
	public static void sendStringList(CommandSender sender, List<String> list) {
		if (!list.isEmpty()) {
			String string = "";
			int i = 0;
			for (String element : list) {
				if (i == 0) string += element;
				else string += ", " + element;
				i++;
			}
			send(sender, string);
		}
	}
	
}