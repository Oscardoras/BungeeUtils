package org.bungeeutils;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

/** Represents a BungeeCord command */
public abstract class BungeeCommand extends Command implements TabExecutor {
	
	/**
	 * Represents a BungeeCord command
	 * @param name the name of the command
	 */
	public BungeeCommand(String name) {
		super(name);
	}
	
	/**
	 * Represents a BungeeCord command
	 * @param name the name of the command
	 * @param permission the permission of the command
	 * @param aliases the aliases of the command
	 */
	public BungeeCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}
	
	/**
	 * Completes the command
	 * @param sender the sender of the command
	 * @param args the arguments of the command
	 * @return the suggestions
	 */
	public abstract List<String> complete(CommandSender sender, String[] args);

	public final Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> arguments = new ArrayList<String>();
		for (String arg : complete(sender, args)) if (arg.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) arguments.add(arg);
		return arguments;
	}
	
}