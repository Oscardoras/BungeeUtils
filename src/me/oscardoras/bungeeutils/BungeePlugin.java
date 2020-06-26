package me.oscardoras.bungeeutils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

/** Represents a Bungee plugin. */
public class BungeePlugin extends Plugin implements Listener {
	
	private final Map<String, Properties> translations = new HashMap<String, Properties>();
	
	/** Represents a Bungee plugin. */
	public BungeePlugin() {
		try {
			JarFile jarFile = new JarFile(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				if (entry.getName().startsWith("translations"))
					try {
						Properties properties = new Properties();
						properties.load(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(entry.getName()), Charset.forName("UTF-8")));
						String[] files = entry.getName().split("/");
						translations.put(files[files.length - 1].split("\\.")[0], properties);
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			jarFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets translation files for this plugin.
	 * @return a map containing the translation properties files for each language defined
	 */
	public Map<String, Properties> getTranslations() {
		return translations;
	}
	
}