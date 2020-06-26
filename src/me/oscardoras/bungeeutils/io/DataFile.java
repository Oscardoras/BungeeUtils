package me.oscardoras.bungeeutils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/** Represents a file containing data */
public class DataFile {
	
	protected final File file;
	protected Object object;
	
	/**
	 * Represents a file containing data
	 * @param path the path of the file
	 */
	public DataFile(String path) {
		this.file = new File(path);
	}
	
	/**
	 * Represents a file containing data
	 * @param file the file
	 */
	public DataFile(File file) {
		this.file = file;
	}
	
	/**
	 * Gets the file
	 * @return the file
	 */
	public File getFile() {
		try {
			file.setReadable(true);
			file.setWritable(true);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			return file;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets the config for this file
	 * @return the YamlConfiguration object
	 */
	public Configuration getAsYaml() {
		try {
			Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getFile());
			object = config;
			return config;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Gets the properties for this file
	 * @return the Properties object
	 */
	public Properties getAsProperties() {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(getFile()));
			object = properties;
			return properties;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/** Saves the file */
	public void save() {
		if (object != null) {
			try {
				if (object instanceof Configuration) ConfigurationProvider.getProvider(YamlConfiguration.class).save((Configuration) object, getFile());
				else ((Properties) object).store(new FileOutputStream(getFile()), "");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof DataFile) {
			try {
				return file.getCanonicalPath().equals(((DataFile) object).file.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
				return file.getPath().equals(((DataFile) object).file.getPath());
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		try {
			hash *= 6 + file.getCanonicalPath().hashCode();
		} catch (IOException e) {
			hash *= 6 + file.getPath().hashCode();
		}
		return hash;
	}
	
}