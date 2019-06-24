package org.bungeeutils;

import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public abstract class BungeeCommand extends Command implements TabExecutor {
	
	public BungeeCommand(String name) {
		super(name);
	}

	public BungeeCommand(String name, String permission, String... aliases) {
		super(name, permission, aliases);
	}
	
	public abstract List<String> complete(CommandSender sender, String[] args);

	public final Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		List<String> arguments = new ArrayList<String>();
		for (String arg : complete(sender, args)) if (arg.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) arguments.add(arg);
		return arguments;
	}
	
}