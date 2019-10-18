package org.bungeeutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bungeeutils.io.DataFile;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;

/** Represents an offline player. */
public class OfflinePlayer {
	
	public final static Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
	private static final DataFile file = new DataFile("config.yml");
	private static final DataFile players = new DataFile("players.yml");
	private final static Map<String, UUID> cache = new HashMap<String, UUID>();
	
	
	protected final String name;
	
	/**
	 * Represents an offline player.
	 * @param name the name of the player
	 */
	public OfflinePlayer(String name) {
		this.name = name;
	}
	
	/**
	 * Represents an offline player.
	 * @param player a proxied player
	 */
	public OfflinePlayer(ProxiedPlayer player) {
		this.name = player.getName();
	}
	
	/**
	 * Gets the name of the player.
	 * @return the name of the player
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Gets the UUID of the player.
	 * @return the UUID of the player
	 */
	public UUID getUUID() {
		Configuration config = players.getAsYaml();
		for (String key : config.getKeys())
			try {
				if (config.contains(key + ".name") && config.getString(key + ".name").equals(name)) return UUID.fromString(key);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) return player.getUniqueId();
		if (BungeeCord.getInstance().config.isOnlineMode()) return getOnlineUUID(name);
		else return getOfflineUUID(name);
	}
	
	/**
	 * Gets if the player is premium.
	 * @return true if the player is premium
	 */
	public boolean isPremium() {
		return getOnlineUUID(name) != null;
	}
	
	/**
	 * Gets the ProxiedPlayer for this player.
	 * @return the ProxiedPlayer for this player
	 */
	public ProxiedPlayer getProxiedPlayer() {
		return ProxyServer.getInstance().getPlayer(name);
	}
	
	/**
	 * Gets if the player is online.
	 * @return true if the player is online
	 */
	public boolean isOnline() {
		return getProxiedPlayer() != null;
	}
	
	/**
	 * Gets the groups of this player.
	 * @return the groups of this player
	 */
	public List<String> getGroups() {
		Configuration config = file.getAsYaml();
		if (config.contains("groups." + name)) return config.getStringList("groups." + name);
		else return new ArrayList<String>();
	}
	
	/**
	 * Adds a group for this player.
	 * @param group the group to add
	 */
	public void addGroup(String group) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) player.addGroups(group);
		List<String> groups = getGroups();
		if (!groups.contains(group)) {
			groups.add(group);
			file.getAsYaml().set("groups." + name, groups);
		    file.save();
		}
	}
	
	/**
	 * Removes a group for this player.
	 * @param group the group to remove
	 */
	public void removeGroup(String group) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) player.removeGroups(group);
		List<String> groups = getGroups();
		if (groups.contains(group)) {
			groups.remove(group);
			file.getAsYaml().set("groups." + name, groups);
		    file.save();
		}
	}
	
	/**
	 * Check if the player has the permission.
	 * @param true if the player has the permission
	 */
	public boolean hasPermission(String permission) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) return player.hasPermission(permission);
		Configuration config = file.getAsYaml();
		List<String> groups = getGroups();
		groups.add("default");
		for (String group : groups) {
			if (config.contains("permissions." + group)) if (config.getStringList("permissions." + group).contains(permission)) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof OfflinePlayer && name.equals(((OfflinePlayer) object).name);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		hash *= 9 + name.hashCode();
		return hash;
	}
	
	
	/**
	 * Checks if Mojang services are online.
	 * @param true Mojang services are online
	 */
	public static boolean getMojangStatus() {
		try {
			JsonArray jsonArray = get("https://status.mojang.com/check").getAsJsonArray();
			for (Object object : jsonArray) {
				JsonObject jsonObject = (JsonObject) object;
				if (jsonObject.has("api.mojang.com") && !jsonObject.get("api.mojang.com").getAsString().equals("green")) return false;
				if (jsonObject.has("sessionserver.mojang.com") && !jsonObject.get("sessionserver.mojang.com").getAsString().equals("green")) return false;
			}
			return true;
		} catch (Exception e) {}
		return false;
	}
	
	protected static UUID getOnlineUUID(String name) {
		if (cache.containsKey(name)) return cache.get(name);
		else {
			if (name != null && namePattern.matcher(name).matches()) {
				try {
					String id = get("https://api.mojang.com/users/profiles/minecraft/" + name).getAsJsonObject().get("id").getAsString();
					UUID uuid = UUID.fromString(id.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
					cache.put(name, uuid);
					return uuid;
				} catch (Exception e) {}
			}
			return null;
		}
	}
	
	protected static UUID getOfflineUUID(String name) {
		if (name != null && namePattern.matcher(name).matches()) {
			UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
			return uuid;
		}
		return null;
	}
	
	protected static JsonElement get(String url) throws MalformedURLException, IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), Charsets.UTF_8))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		
		return new JsonParser().parse(stringBuilder.toString());
	}
	
}