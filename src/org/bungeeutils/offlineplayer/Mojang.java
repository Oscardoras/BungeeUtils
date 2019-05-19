package org.bungeeutils.offlineplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Mojang {
	private Mojang() {}
	
	public final static Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
	
	private static JsonElement get(String url) throws MalformedURLException, IOException {
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new URL(url).openStream(), Charsets.UTF_8))) {	
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		
		return new JsonParser().parse(stringBuilder.toString());
	}
	
	public static boolean getStatus() {
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
	
	public static UUID getOnlineUUID(String name) throws NotExistPlayerException {
		if (name != null && namePattern.matcher(name).matches()) {
			try {
				String id = get("https://api.mojang.com/users/profiles/minecraft/" + name).getAsJsonObject().get("id").getAsString();
				return UUID.fromString(id.replaceFirst("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
			} catch (Exception ex) {}
		}
		throw new NotExistPlayerException();
	}
	
	public static UUID getOfflineUUID(String name) throws NotExistPlayerException {
		if (name != null && namePattern.matcher(name).matches()) return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes());
		throw new NotExistPlayerException();
	}
	
}