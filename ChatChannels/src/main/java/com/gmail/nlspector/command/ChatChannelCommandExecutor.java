package com.gmail.nlspector.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChatChannelCommandExecutor {
	//This class is just an archetype for all command executors
	ChatColor error;
	ChatColor secondary;
	ChatColor primary;
	ChatChannel plugin;
	int nickMaxLength;
	int tuneMessage = 0;
	int joinMessage = 1;
	int leaveMessage = 2;
	int untuneMessage = 3;
	
	public ChatChannelCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		primary = pCS;
		secondary = sCS;
		error = eC;
		plugin = c;
		nickMaxLength = nickMaxLen;
	}
	
	public FileConfiguration getConfig() {
		return plugin.getConfig();
	}
	
	public FileConfiguration getCurrentChannel() {
		return plugin.getCurrentChannel();
	}
	
	public FileConfiguration getNickname() {
		return plugin.getNickname();
	}
	
	public void saveConfig() {
		 plugin.saveConfig();
	}
	
	public void saveCurrentChannel() {
		 plugin.saveCurrentChannel();
	}
	
	public void saveNickname() {
		 plugin.saveNickname();
	}
	
	//Helper methods grandfathered in from the main class
	public String getUUIDByName(String name){
		String UUID = "Error";
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			UUID = player.getName().equals(name) ? player.getUniqueId().toString() : "Error";
			if(!UUID.equals("Error")){
				return UUID;
			}
		}
		return "Error";
	}
	
	public boolean isTrusted(String channelname, Player player) {
		return getConfig().getStringList("ctrusted." + channelname).contains(player.getUniqueId().toString());
	}
	
	public void sendEntryExitMessages(CommandSender sender, Command cmd, String label, String[] args, int messageType, String channel) {
		String senderName = ((Player) sender).getUniqueId().toString();
		if(!(getNickname().getString(senderName) == null)){
			String nickname = getNickname().getString(senderName);
			senderName = nickname;
		} else {
			senderName = sender.getName();
		}
		boolean isLeaving = false;
		switch(messageType){
		//deal with tuning first. Why, I don't know. 
			case 3:
				//The message will be identical save for the negative or positive, so allow it to fall through with only the boolean being toggled. Much less lines than the alternative (copy and paste)
				isLeaving = true;
			case 0:
				if(getConfig().getString("cflags." + channel + ".tune-messages").equals("none")) {
					return;
				} else if(getConfig().getString("cflags." + channel + ".tune-messages").equals("all")) {
					for(Player player : Bukkit.getServer().getOnlinePlayers()){
						String playerID = player.getUniqueId().toString();
						String playerCurrentChannel = getCurrentChannel().getString(playerID);
						if((playerCurrentChannel.equalsIgnoreCase(channel) || getCurrentChannel().getStringList("ctuned." + channel).contains(player.getUniqueId().toString()))){
							player.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
						} 
					}
					return;	
				} else if(getConfig().getString("cflags." + channel + ".tune-messages").equals("owner-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
					} else {
						return;
					}
				} else if(getConfig().getString("cflags." + channel + ".tune-messages").equals("trusted-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
					} 
					for(String s : getConfig().getStringList("ctrusted." + channel)) {
						Player trusted = Bukkit.getServer().getPlayer(UUID.fromString(s));
						if(trusted != null) {
							trusted.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
						}
					}
					return;
				
				}
				break;
			//copy and paste for joining/leaving
			case 2:
				//The message will be identical save for the negative or positive, so allow it to fall through with only the boolean being toggled. Much less lines than the alternative (copy and paste)
				isLeaving = true;
			case 1:
				if(getConfig().getString("cflags." + channel + ".join-messages").equals("none")) {
					return;
				} else if(getConfig().getString("cflags." + channel + ".join-messages").equals("all")) {
					for(Player player : Bukkit.getServer().getOnlinePlayers()){
						String playerID = player.getUniqueId().toString();
						String playerCurrentChannel = getCurrentChannel().getString(playerID);
						if((playerCurrentChannel.equalsIgnoreCase(channel) || getCurrentChannel().getStringList("ctuned." + channel).contains(player.getUniqueId().toString()))){
							player.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has " + (isLeaving ? "left" : "joined") + " the channel.");
						} 
					}
					return;	
				} else if(getConfig().getString("cflags." + channel + ".join-messages").equals("owner-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has " + (isLeaving ? "left" : "joined") + " the channel.");
					} else {
						return;
					}
				} else if(getConfig().getString("cflags." + channel + ".join-messages").equals("trusted-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has " + (isLeaving ? "left" : "joined") + " the channel.");
					} 
					for(String s : getConfig().getStringList("ctrusted." + channel)) {
						Player trusted = Bukkit.getServer().getPlayer(UUID.fromString(s));
						if(trusted != null) {
							trusted.sendMessage(primary + ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " has " + (isLeaving ? "left" : "joined") + " the channel.");
						}
					}
					return;
				
				}
			break;
		}
	}
	
	public List<String> makeEquivalent(List<String> toBeCloned, List<String> target) {
		target.clear();
		for(String t : toBeCloned) {
			target.add(t);
		}
		return target;
	}
	
}
