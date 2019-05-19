package org.bungeeutils.offlineplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bungeeutils.io.DataFile;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

public class OfflinePlayer {
	
	static public Map<String, UUID> uuids = new HashMap<String, UUID>();
	static private final DataFile file = new DataFile("config.yml");
	
	protected String name;
	
	public OfflinePlayer(String name) {
		this.name = name;
	}
	
	public OfflinePlayer(ProxiedPlayer player) {
		this.name = player.getName();
	}
	
	public String getName() {
		return name;
	}
	
	public UUID getUUID() throws NotExistPlayerException {
		if (uuids.containsKey(name)) return uuids.get(name);
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) return player.getUniqueId();
		if (BungeeCord.getInstance().config.isOnlineMode()) return Mojang.getOnlineUUID(name);
		else return Mojang.getOfflineUUID(name);
	}
	
	public boolean isPremium() {
		try {
			Mojang.getOnlineUUID(name);
			return true;
		} catch (NotExistPlayerException ex) {
			return false;
		}
	}
	
	public ProxiedPlayer getProxiedPlayer() {
		return ProxyServer.getInstance().getPlayer(name);
	}
	
	public boolean isOnline() {
		return ProxyServer.getInstance().getPlayer(name) != null;
	}
	
	public List<String> getGroups() {
		Configuration config = file.getYML();
		if (config.contains("groups." + getName())) return config.getStringList("groups." + getName());
		else return new ArrayList<String>();
	}
	
	public void addGroup(String group) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) player.addGroups(group);
		List<String> groups = getGroups();
		if (!groups.contains(group)) {
			groups.add(group);
			file.getYML().set("groups." + getName(), groups);
		    file.save();
		}
	}
	
	public void removeGroup(String group) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) player.removeGroups(group);
		List<String> groups = getGroups();
		if (groups.contains(group)) {
			groups.remove(group);
			file.getYML().set("groups." + getName(), groups);
		    file.save();
		}
	}
	
	public boolean hasPermission(String permission) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) return player.hasPermission(permission);
		Configuration config = file.getYML();
		List<String> groups = getGroups();
		groups.add("default");
		for (String group : groups) {
			if (config.contains("permissions." + group)) if (config.getStringList("permissions." + group).contains(permission)) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null) {
			if (object instanceof OfflinePlayer) return name.equals(((OfflinePlayer) object).name);
			if (object instanceof ProxiedPlayer) return name.equals(((ProxiedPlayer) object).getName());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
}