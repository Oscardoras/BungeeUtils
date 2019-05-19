package org.bungeeutils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class DataFile {
    
    private final static List<FileCache<Configuration>> yamlConfigurationFiles = reloadYamlConfiguration();
	private static List<FileCache<Configuration>> reloadYamlConfiguration() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				try {
				    synchronized(yamlConfigurationFiles) {
    					List<FileCache<Configuration>> newConfig = new ArrayList<FileCache<Configuration>>();
    					for (FileCache<Configuration> fileCache : yamlConfigurationFiles) {
    						Configuration config = fileCache.get();
    						if (config != null) {
    							try {
    								config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fileCache.dataFile.getFile());
    								newConfig.add(new FileCache<Configuration>(fileCache.dataFile, config));
    							} catch (Exception ex) {
    								ex.printStackTrace();
    							}
    						}
    					}
    					yamlConfigurationFiles.clear();
    					yamlConfigurationFiles.addAll(newConfig);
				    }
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}, 5000l, 5000l);
		return new ArrayList<FileCache<Configuration>>();
	}
	
	private final static List<FileCache<Properties>> propertiesFiles = reloadProperties();
	private static List<FileCache<Properties>> reloadProperties() {
		new Timer().schedule(new TimerTask() {
			public void run() {
				try {
				    synchronized(propertiesFiles) {
    					Iterator<FileCache<Properties>> propertiesIt = propertiesFiles.iterator();
    					while (propertiesIt.hasNext()) {
    					    FileCache<Properties> fileCache = propertiesIt.next();
    						Properties properties = fileCache.get();
    						if (properties != null) {
    							try {
    								properties.load(new FileInputStream(fileCache.dataFile.getFile()));
    							} catch (Exception ex) {
    								ex.printStackTrace();
    							}
    						} else {
    							propertiesIt.remove();
    						}
    					}
				    }
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}, 5000l, 5000l);
		return new ArrayList<FileCache<Properties>>();
	}
	
	
	protected File file;
	
	public DataFile(String path) {
		this.file = new File(path);
	}
	
	public DataFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		try {
			file.setReadable(true);
			file.setWritable(true);
			if (!file.isFile()) {
				if (file.exists()) file.delete();
				file.mkdirs();
				file.delete();
				file.createNewFile();
			}
			return file;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public Configuration getYML() {
		try {
			Configuration config = null;
			synchronized(yamlConfigurationFiles) {
    			if (yamlConfigurationFiles.contains(this)) {
    				config = yamlConfigurationFiles.get(yamlConfigurationFiles.indexOf(this)).get();
    			}
    			if (config == null) {
    				config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(getFile());
    				yamlConfigurationFiles.remove(this);
    				yamlConfigurationFiles.add(new FileCache<Configuration>(this, config));
    			}
			}
			return config;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public Properties getProperties() {
		try {
			Properties properties = null;
			synchronized(propertiesFiles) {
    			if (propertiesFiles.contains(this)) {
    				properties = propertiesFiles.get(propertiesFiles.indexOf(this)).get();
    			}
    			if (properties == null) {
    				properties = new Properties();
    				properties.load(new FileInputStream(getFile()));
    				propertiesFiles.remove(this);
    				propertiesFiles.add(new FileCache<Properties>(this, properties));
    			}
			}
			return properties;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unlikely-arg-type")
	public void save() {
		try {
		    synchronized(yamlConfigurationFiles) {
    			if (yamlConfigurationFiles.contains(this)) {
    				Configuration config = yamlConfigurationFiles.get(yamlConfigurationFiles.indexOf(this)).get();
    				if (config != null) ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, getFile());
    				return;
    			}
		    }
			synchronized(propertiesFiles) {
    			if (propertiesFiles.contains(this)) {
    				Properties properties = propertiesFiles.get(propertiesFiles.indexOf(this)).get();
    				if (properties != null) properties.store(new FileOutputStream(getFile()), "");
    				return;
    			}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null) {
			try {
				if (object instanceof DataFile) return getFile().getCanonicalPath().equals(((DataFile) object).getFile().getCanonicalPath());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			if (object instanceof FileCache<?>) return this.equals(((FileCache<?>) object).dataFile);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		try {
			return getFile().getCanonicalPath().hashCode();
		} catch (IOException ex) {
			ex.printStackTrace();
			return getFile().getPath().hashCode();
		}
	}
	
}

class FileCache<T> extends SoftReference<T> {
	
	public DataFile dataFile;

	public FileCache(DataFile dataFile, T data) {
		super(data);
		this.dataFile = dataFile;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null) {
			if (object instanceof FileCache<?>) return dataFile.equals(((FileCache<?>) object).dataFile);
			if (object instanceof DataFile) return dataFile.equals(object);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return dataFile.hashCode();
	}
	
}