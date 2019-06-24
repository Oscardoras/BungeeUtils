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

public class OfflinePlayer {
	
	public final static Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
	private final static Map<String, UUID> cache = new HashMap<String, UUID>();
	private static final DataFile file = new DataFile("config.yml");
	private static final DataFile players = new DataFile("players.yml");
	
	
	protected final String name;
	
	public OfflinePlayer(String name) {
		this.name = name;
	}
	
	public OfflinePlayer(ProxiedPlayer player) {
		this.name = player.getName();
	}
	
	public String getName() {
		return name;
	}
	
	public UUID getUUID() {
		Configuration config = players.getYML();
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
	
	public boolean isPremium() {
		return getOnlineUUID(name) != null;
	}
	
	public ProxiedPlayer getProxiedPlayer() {
		return ProxyServer.getInstance().getPlayer(name);
	}
	
	public boolean isOnline() {
		return getProxiedPlayer() != null;
	}
	
	public List<String> getGroups() {
		Configuration config = file.getYML();
		if (config.contains("groups." + name)) return config.getStringList("groups." + name);
		else return new ArrayList<String>();
	}
	
	public void addGroup(String group) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) player.addGroups(group);
		List<String> groups = getGroups();
		if (!groups.contains(group)) {
			groups.add(group);
			file.getYML().set("groups." + name, groups);
		    file.save();
		}
	}
	
	public void removeGroup(String group) {
		ProxiedPlayer player = getProxiedPlayer();
		if (player != null) player.removeGroups(group);
		List<String> groups = getGroups();
		if (groups.contains(group)) {
			groups.remove(group);
			file.getYML().set("groups." + name, groups);
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
	
	
	public static boolean getMojangStatus() {
		try {
			JsonArray jsonArray = get("https://status.mojang.com/check").getAsJsonArray();
			for (Object object : jsonArray) {
				JsonObject jsonObject = (JsonObject) object;
				if (jsonObject.has("api.mojang.com") && !jsonObject.get("api.mojang.com").getAsString().equals("green")) return false;
				if (jsonObject.has("sessionserver.mojang.com") && !jsonObject.get("sessionserver.mojang.com").getAsString().equals("green")) return false;
			}
			return true;
		} catch (Exception ex) {}
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
				} catch (Exception ex) {}
			}
			return null;
		}
	}
	
	protected static UUID getOfflineUUID(String name) {
		if (cache.containsKey(name)) return cache.get(name);
		else {
			if (name != null && namePattern.matcher(name).matches()) {
				UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
				cache.put(name, uuid);
				return uuid;
			}
			return null;
		}
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