package com.gmail.nlspector.chatchannels;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class CustomConfig {
	
	static File customConfigFile;
	static FileConfiguration customConfig;
	
	public static void reloadCustomConfig(String name){
		if(customConfigFile == null){
			customConfigFile = new File(ChatChannel.plugin.getDataFolder(), name + ".yml");
		}
		customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
	}
	
	public static FileConfiguration getCustomConfig(String name) {
		if(customConfig == null){
			reloadCustomConfig(name + ".yml");
		}
		customConfigFile = null;
		return customConfig;
	} 
	
	public static void saveCustomConfig(String name){
		if (customConfig == null || customConfigFile == null) {
	        return;
	    }
		try {
	        getCustomConfig(name + ".yml").save(customConfigFile);
	    } catch (IOException ex) {
	        ChatChannel.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
	    }
		customConfig = null;
		customConfigFile = null;
	}
	public static void saveDefaultCustomConfig(String name) {
	    if (customConfigFile == null) {
	        customConfigFile = new File(ChatChannel.plugin.getDataFolder(), name + ".yml");
	    }
	    if (!customConfigFile.exists()) {            
	         ChatChannel.plugin.saveResource(name + ".yml", false);
	    }
		customConfig = null;
		customConfigFile = null;
	}


}
