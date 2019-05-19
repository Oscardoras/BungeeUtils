package org.bungeeutils;

import java.util.List;

import org.bungeeutils.io.Message;
import org.bungeeutils.io.Translate;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;

public final class SendMessage {
	private SendMessage() {}
	
	public static void send(CommandSender sender, String message) {
		sender.sendMessage(new TextComponent(ChatColor.RESET + message));
	}
	
	public static void out(String message) {
		send(ProxyServer.getInstance().getConsole(), message);
	}
	
	public static void send(CommandSender sender, Message message, String... args) {
		send(sender, Translate.getPluginMessage(sender, message, args));
	}
	
	public static void sendStringList(CommandSender sender, List<String> list) {
		if (!list.isEmpty()) {
			String string = "";
			int i = 0;
			for (String element : list) {
				if (i == 0) {
					string += element;
				} else {
					string += ", " + element;
				}
				i++;
			}
			send(sender, string);
		}
	}
	
}