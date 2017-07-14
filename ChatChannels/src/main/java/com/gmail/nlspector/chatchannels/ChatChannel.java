package com.gmail.nlspector.chatchannels;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.chat.Chat;

public class ChatChannel extends JavaPlugin implements Listener {

	String primaryChannelSwitch;
	String secondaryChannelSwitch;
	String errorColor;
	int i = 1;
	int tuneMessage = 0;
	int joinMessage = 1;
	int leaveMessage = 2;
	int untuneMessage = 3;
	int nickMaxLength;
	public static ChatChannel plugin;
	public Chat chat;
	Map<String, Boolean> spyMap = new HashMap<>();
	Map<String, Boolean> muteMap = new HashMap<>();
	Map<String, String[]> ignoreMap = new HashMap<>();
	
	@Override
	public void onEnable(){
		plugin = this;
		saveDefaultConfig();
		getConfig();
		getServer().getPluginManager().registerEvents(this, this);
		primaryChannelSwitch = getConfig().getString("colors.PrimaryChannelSwitch");
		secondaryChannelSwitch = getConfig().getString("colors.SecondaryChannelSwitch");
		errorColor = this.getConfig().getString("colors.error");
		nickMaxLength = getConfig().getInt("nick-max-length");
		saveDefaultNickname();
		saveDefaultCurrentChannel();
		setupChat();
		boolean errorsExist = false;
		//Notify the console user if the config is messed up.
		if(!getConfig().getStringList("channels").contains(getConfig().getString("default_channel"))) {
			errorsExist = true;
			getLogger().severe("Error in the config file - the default channel is set to a channel that doesn't exist! Are all the letters lowercase?");
		}
		
		if(errorsExist) getLogger().info("ChatChannels v0.4.0-BETA is enabled with errors!");
		else getLogger().info("ChatChannels v0.4.0-BETA is enabled!");
		
	}

	//get custom configs
	 File nicknameFile;
	 FileConfiguration nickname;
	//start custom config spam
	public void reloadNickname(){
		if(nicknameFile == null){
			nicknameFile = new File(getDataFolder(), "nicknames.yml");
		}
		nickname = YamlConfiguration.loadConfiguration(nicknameFile);
	}
	
	public FileConfiguration getNickname() {
		if(nickname == null){
			reloadNickname();
		}
		
		return nickname;
	} 
	
	public void saveNickname(){
		if (nickname == null || nicknameFile == null) {
	    return;
	  }
		try {
	    getNickname().save(nicknameFile);
	  } catch (IOException ex) {
	    getLogger().log(Level.SEVERE, "Could not save config to " + nicknameFile, ex);
	  }
		
		
	}
	
	public void saveDefaultNickname() {
	  if (nicknameFile == null) {
	    nicknameFile = new File(getDataFolder(), "nicknames.yml");
	  }
	  if (!nicknameFile.exists()) {      
	     saveResource("nicknames.yml", false);
	  }
	}

	 File currentchannelFile;
	 FileConfiguration currentchannel;
	
	public void reloadCurrentChannel(){
		if(currentchannelFile == null){
			currentchannelFile = new File(getDataFolder(), "currentchannels.yml");
		}
		currentchannel = YamlConfiguration.loadConfiguration(currentchannelFile);
	}
	
	public FileConfiguration getCurrentChannel() {
		if(currentchannel == null){
			reloadCurrentChannel();
		}
		return currentchannel;
	} 
	
	public void saveCurrentChannel(){
		if (currentchannel == null || currentchannelFile == null) {
	    return;
	  }
		try {
	    getCurrentChannel().save(currentchannelFile);
	  } catch (IOException ex) {
	    getLogger().log(Level.SEVERE, "Could not save config to " + currentchannelFile, ex);
	  }
		
		
	}
	
	public void saveDefaultCurrentChannel() {
	  if (currentchannelFile == null) {
	    currentchannelFile = new File(getDataFolder(), "currentchannels.yml");
	  }
	  if (!currentchannelFile.exists()) {      
	     saveResource("currentchannels.yml", false);
	  }
		
		
	}
	//end custom config spam
	
	//This function... is very long.
	//And uncommented. I'll be working on that.
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("channel")){
			return doChannelSwitch(sender, cmd, label, args);
		} else if(cmd.getName().equalsIgnoreCase("cnick")){
			return doCnick(sender, cmd, label, args);
		} else if(cmd.getName().equalsIgnoreCase("cg")){
			String senderUUID = getUUIDByName(sender.getName());
			String senderName = "";
			if(!(getNickname().getString(senderUUID) == null)){
				String nickname = getNickname().getString(senderName);
				senderName = nickname;
			} else {
				senderName = sender.getName();
			}
			if(args.length < 1){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to type a message!");
			} else if(muteMap.get(senderUUID) == true){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are muted!");
				return true;
			} else {
				StringBuilder sb = new StringBuilder();
				for(i = 0; i < args.length; i++){
					sb.append(args[i] + " ");
				}
				String message = sb.toString();
				String prefix = chat.getPlayerPrefix((Player) sender);
				String suffix = chat.getPlayerSuffix((Player) sender);
				prefix = (prefix.length() > 0) ? prefix + " ": "";
				suffix = (suffix.length() > 0) ? " " + suffix + " ": "";
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					boolean isIgnored = false;

					if(ignoreMap.containsKey(senderUUID)){
						String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
						for(String s : ignoreList){
							if(s.equals(getUUIDByName(sender.getName()))){
								isIgnored = true;
							}
						}
					}
					if(!isIgnored) {
						if(sender.hasPermission("chatchannels.message.color")) {
							player.sendMessage(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
						} else {
							player.sendMessage(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
						}
					}
				}
			}
			return true;
		} else if(cmd.getName().equals("newchannel")){
			if(args.length < 1){
				sender.sendMessage("You need to specify a name!");
				return false;
			} else {
				String newChannel = args[0].toLowerCase();
					if(getConfig().getStringList("channels").contains(newChannel) || getConfig().getStringList("invite-only-channels").contains(newChannel)){
						sender.sendMessage(ChatColor.valueOf(errorColor) + "That channel exists already!");
					} else {
						
						List<String> empty = new ArrayList<>();
						//Moved addition to channel list to the end - in case of invite only.
						getConfig().set("passcodes." + newChannel, "default");
						getConfig().set("channelcolor." + newChannel, "WHITE");
						getConfig().set("cowner." + newChannel, ((Player) sender).getUniqueId().toString());
						getConfig().set("ctrusted." + newChannel, empty);
						getConfig().set("cban." + newChannel, empty);
						getConfig().set("cflags." + newChannel + ".tune-messages", "all");
						getConfig().set("cflags." + newChannel + ".join-messages", "all");
						getConfig().set("cflags." + newChannel + ".allow-tune", "all");
						saveConfig();
						getCurrentChannel().set("ctuned." + newChannel, empty);
						saveCurrentChannel();
						sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have created a new channel named " + ChatColor.valueOf(primaryChannelSwitch) + newChannel);							
						if(args.length == 2){
							
							if(args[1].equals("invite-only")) {
								if(!sender.hasPermission("chatchannels.create.invite-only")) {
									sender.sendMessage(ChatColor.valueOf(errorColor) + "You don't have permission to create an invite-only channel!");
									List<String> s = getConfig().getStringList("channels");
									s.add(newChannel);
									getConfig().set("channels", s);
									saveConfig();
									return true;
								}
								List<String> s = getConfig().getStringList("invite-only-channels");
								s.add(newChannel);
								getConfig().set("invite-only-channels", s);
								List<String> invitees = new ArrayList<>();
								invitees.add(((Player) sender).getUniqueId().toString());
								getConfig().set("invitees." + newChannel, invitees);
								saveConfig();
								sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Your channel is " + ChatColor.valueOf(primaryChannelSwitch) + "invite only.");
								return true;
							}
							if(!sender.hasPermission("chatchannels.create.passcode")) {
								sender.sendMessage(ChatColor.valueOf(errorColor) + "You don't have permission to create an password-protected channel!");
							} else {
								getConfig().set("passcodes." + newChannel, args[1]);
								saveConfig();
								sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Your channel has a passcode: " + ChatColor.valueOf(primaryChannelSwitch) + args[1]);
							}
						}
						List<String> s = getConfig().getStringList("channels");
						s.add(newChannel);
						getConfig().set("channels", s);
						saveConfig();
					}
				return true;
			}
		} else if(cmd.getName().equals("clist")){
			String channellist = getConfig().getStringList("channels").toString();
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The available channels are: " + ChatColor.valueOf(primaryChannelSwitch) + channellist.substring(1, channellist.length()-1));
			return true;
		} else if(cmd.getName().equals("cedit")){
			doCedit(sender, cmd, label, args);
			return true;
		} else if(cmd.getName().equals("cplayers")){
			String senderCurrentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			if(senderCurrentChannel.equals("noChannel")){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to be in a channel to list the people in it.");
				return false;
			}
			StringBuilder cplayerlist = new StringBuilder();
			if(sender instanceof Player){
				for(Player player: Bukkit.getServer().getOnlinePlayers()){
					String playerCurrentChannel = getCurrentChannel().getString(player.getUniqueId().toString());
					if(playerCurrentChannel.equalsIgnoreCase(senderCurrentChannel)){
						String nickname = player.getName();
						cplayerlist.append(nickname + ", ");
					}
				}
				cplayerlist.delete(cplayerlist.length() - 2, cplayerlist.length() - 1);
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The players in your channel are: " + ChatColor.valueOf(primaryChannelSwitch) + cplayerlist.toString());
				return true;
			} else {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You can't list players in your channel if you are not a player yourself!");
				return false;
			}
		} else if(cmd.getName().equals("cspy")){
			if(sender instanceof Player){
				String UUID = ((Player) sender).getUniqueId().toString();
				boolean isSpying = spyMap.get(UUID);
				spyMap.put(UUID, !isSpying);
				String spyModeStatus = !isSpying ? "on" : "off";
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have turned spy mode " + ChatColor.valueOf(primaryChannelSwitch) + spyModeStatus + ".");
				return true;
			} else {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You already have /cspy, Console user!");
				return false;
			}
		} else if(cmd.getName().equals("cmute")){
			if(args.length > 0){
				String pName = "Error";
				isRealPlayer:
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					if(player.getName().equals(args[0])){
						pName = args[0];
						break isRealPlayer;
					}
					pName = "Error";
				}
				if(pName.equals("Error")){
					sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify a valid player!");
					return false;
				}
				String UUID = getUUIDByName(args[0]);
				boolean isMuted = muteMap.get(UUID);
				muteMap.put(UUID, !isMuted);
				String muteStatus = !isMuted ? "muted" : "unmuted";
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have " + ChatColor.valueOf(primaryChannelSwitch) + muteStatus  + " " + args[0] + ".");
				return true;
			} else {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to specify a player!");
				return false;
			}
		} else if(cmd.getName().equals("crealname")){
			if(args.length < 1){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to specify a player!");
				return false;
			}
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				String plainNick = getNickname().getString(p.getUniqueId().toString());
				char[] plainNickArray = plainNick.toCharArray();
				StringBuilder sb = new StringBuilder();
				for(i = 1; i < plainNickArray.length; i++){
					if(!(plainNickArray[i] == '&')){
						sb.append(plainNickArray[i]);
					} else {
						i++;
					}
					plainNick = sb.toString();
				}
				if(plainNick.equalsIgnoreCase(args[0])){
					sender.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + p.getName() + ChatColor.valueOf(secondaryChannelSwitch) + " has the nickname: " + getNickname().getString(p.getUniqueId().toString()));
					return true;
				}
			}
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That player is not on or that is not a real nickname.");
		} else if(cmd.getName().equals("cignore")){
			if(args.length < 1) sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to specify who you want to ignore!");
			else if(!(sender instanceof Player)) sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to be a player to ignore someone!");
			else {
				String[] ignoreList;
				if(!ignoreMap.containsKey(getUUIDByName(sender.getName()))) {
					ignoreList = new String[1];
					ignoreList[0] = getUUIDByName(args[0]);
					ignoreMap.put(getUUIDByName(sender.getName()), ignoreList);
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have ignored " + ChatColor.valueOf(primaryChannelSwitch) + args[0]);
					return true;
				}
				else{
					ignoreList = new String[ignoreMap.get(getUUIDByName(sender.getName())).length + 1];
					for(i = 0; i < ignoreList.length - 1; i++){
						ignoreList[i] = ignoreMap.get(getUUIDByName(sender.getName()))[i];
					}
					for(i = 0; i < ignoreList.length - 1; i++){
						if(ignoreList[i].equals(getUUIDByName(args[0]))){
							ignoreList[i] = ignoreList[ignoreList.length - 1];
							ignoreList[ignoreList.length - 1] = null;
							ignoreMap.put(getUUIDByName(sender.getName()), ignoreList);
							sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have unignored " + ChatColor.valueOf(primaryChannelSwitch) + args[0]);
							//garbage collection :D
							if(ignoreList[0] == null) ignoreMap.remove(getUUIDByName(sender.getName()));
							return true;
						} 
					} 
					ignoreList[ignoreList.length] = getUUIDByName(args[0]);
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have ignored " + ChatColor.valueOf(primaryChannelSwitch) + args[0]);
				}
				ignoreMap.put(getUUIDByName(sender.getName()), ignoreList);
				return true;
			}
		} else if(cmd.getName().equals("ctrust")) {
			//handle miscreants
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not a player in a channel!");
				return true;
			}
			String currentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			//in English - if the owner of the current channel isn't the player sending the command... 
			if(!getConfig().getString("cowner." + currentChannel).equals(((Player) sender).getUniqueId().toString())) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not the owner of the channel!");
				return true;
			}
			if(args.length == 0) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify which player to trust!");
				return true;
			}
			//now to trust people
			List<String> s = getConfig().getStringList("ctrusted." + currentChannel);
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				if(player.getName().equals(args[0])) {
					if(s.contains(player.getUniqueId().toString())) {
						s.remove(player.getUniqueId().toString());
						sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have " + ChatColor.valueOf(primaryChannelSwitch) + "untrusted " + args[0] + ".");
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have been " + ChatColor.valueOf(primaryChannelSwitch) + "untrusted on " + currentChannel + ".");
					} else {
						s.add(player.getUniqueId().toString());
						sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have " + ChatColor.valueOf(primaryChannelSwitch) + "trusted " + args[0]);
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have been " + ChatColor.valueOf(primaryChannelSwitch) + "trusted on " + currentChannel + ".");

					}
					getConfig().set("ctrusted." + currentChannel, s);
					saveConfig();
					return true;
				}
			}
		} else if(cmd.getName().equals("cowner")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not a player in a channel!");
				return true;
			}
			String currentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			String owner = getConfig().getString("cowner." + currentChannel);
			owner = (owner.equals("no_owner")) ? owner : (Bukkit.getPlayer(UUID.fromString(owner)).getName());
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The owner of this channel is " + ChatColor.valueOf(primaryChannelSwitch) + owner + ".");
			return true;
		} else if(cmd.getName().equals("cdel")) {
			//copy and paste mostly from ctrust
			//handle miscreants
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not a player in a channel!");
				return true;
			}
			
			String selchannel = args[0];
			if(!(getConfig().getStringList("channels").contains(selchannel) && getConfig().getStringList("invite-only-channels").contains(selchannel))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "That channel doesn't exist!");
				return true;
			}
			//in English - if the owner of the current channel isn't the player sending the command nor has permission to override... 
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || sender.hasPermission("chatchannels.override.delete"))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to delete this channel!");
				return true;
			}
			if(selchannel.equals(getConfig().getString("default_channel"))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You can't delete the default channel!");
				return true;
			}
			if(args.length == 0) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify which channel to delete!");
				return true;
			}
			
			List<String> s = getConfig().getStringList("channels");
			s.remove(args[0]);
			getConfig().set("channels", s);
			saveConfig();
			//kick everyone else off
			String defaultChannel = getConfig().getString("default_channel");
			for(Player player: Bukkit.getServer().getOnlinePlayers()){
				String playerCurrentChannel = getCurrentChannel().getString(player.getUniqueId().toString());
				if(playerCurrentChannel.equalsIgnoreCase(selchannel)){
					player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The channel you were on was deleted! Moving you to: " + ChatColor.valueOf(primaryChannelSwitch) + defaultChannel);
					getCurrentChannel().set(player.getUniqueId().toString(), defaultChannel);
					saveCurrentChannel();
				}
			}
			if(args.length < 1) {
				if(args[1].equalsIgnoreCase("purge") && sender.hasPermission("chatchannels.delete.purge")) {
					getConfig().set("passcodes." + selchannel, null);
					getConfig().set("channelcolor." + selchannel, null);
					getConfig().set("cowner." + selchannel, null);
					getConfig().set("ctrusted." + selchannel, null);
					getConfig().set("cban." + selchannel, null);
					getConfig().set("cflags." + selchannel, null);
					saveConfig();
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have purged the channel " + ChatColor.valueOf(primaryChannelSwitch) + args[0] + ".");
					return true;
				} else if(args[1].equalsIgnoreCase("purge") && !sender.hasPermission("chatchannels.delete.purge")) {
					sender.sendMessage(ChatColor.valueOf(errorColor) + "You don't have permission to purge this channel.");
				}
			}
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have deleted the channel " + ChatColor.valueOf(primaryChannelSwitch) + args[0] + ".");
			return true;
		} else if(cmd.getName().equals("cban")) {
			//deal with the permissions
			if(args.length == 0) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify a player to ban!");
				return false;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must be a player to ban someone from a channel!");
				return false;
			}
			String selchannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			if(selchannel.equals(getConfig().getString("default_channel"))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You can't ban someone from the default channel!");
				return true;
			}
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.override.ban"))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to ban someone from the channel!");
				return true;
			}

			String defaultChannel = getConfig().getString("default_channel");
			for(Player player: Bukkit.getServer().getOnlinePlayers()){
				if(player.getName().equals(args[0])){
					List<String> s = getConfig().getStringList("cban." + selchannel);
					s.add(player.getUniqueId().toString());
					getConfig().set("cban." + selchannel, s);
					saveConfig();
					if(getCurrentChannel().getString(player.getUniqueId().toString()).equals(selchannel)) {
						sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(player.getUniqueId().toString()));
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You were banned from the channel: " + ChatColor.valueOf(primaryChannelSwitch) + selchannel + ChatColor.valueOf(secondaryChannelSwitch) + "! Moving you to: " + ChatColor.valueOf(primaryChannelSwitch) + defaultChannel);		
						getCurrentChannel().set(player.getUniqueId().toString(), defaultChannel);
						saveCurrentChannel();
					} else if (getCurrentChannel().getStringList("ctuned." + selchannel).contains(player.getUniqueId().toString())){
						sendEntryExitMessages(sender, cmd, label, args, untuneMessage, getCurrentChannel().getString(player.getUniqueId().toString()));
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You were banned from the channel: " + ChatColor.valueOf(primaryChannelSwitch) + selchannel + ChatColor.valueOf(secondaryChannelSwitch) + "! Tuning you out...");
						List<String> s2 = getConfig().getStringList("ctuned." + selchannel);
						s2.remove(player.getUniqueId().toString());
						getCurrentChannel().set("ctuned." + selchannel, s2);
						saveCurrentChannel();
					} else {
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You were banned from the channel: " + ChatColor.valueOf(primaryChannelSwitch) + selchannel + ChatColor.valueOf(secondaryChannelSwitch) + "!");
					}
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have banned " + ChatColor.valueOf(primaryChannelSwitch) + args[0] + ChatColor.valueOf(secondaryChannelSwitch) + " from the channel.");
					
					return true;
				}
			}
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That isn't a player!");
			return true;
			
		} else if(cmd.getName().equals("cpardon")) {
			//deal with the permissions
			if(args.length == 0) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify a player to pardon!");
				return false;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must be a player to pardon someone from a channel!");
				return false;
			}
			String selchannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			if(selchannel == getConfig().getString("default_channel")) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You can't pardon someone from the default channel!");
				return true;
			}
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.override.ban"))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to pardon someone from this channel!");
				return true;
			}
			for(Player player: Bukkit.getServer().getOnlinePlayers()){
				if(player.getName().equals(args[0])){
					List<String> s = getConfig().getStringList("cban." + selchannel);
					s.remove(player.getUniqueId().toString());
					getConfig().set("cban." + selchannel, s);
					saveConfig();
					player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You were unbanned from the channel: " + ChatColor.valueOf(primaryChannelSwitch) + selchannel + ChatColor.valueOf(secondaryChannelSwitch) + "!");
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have unbanned " + ChatColor.valueOf(primaryChannelSwitch) + args[0] + ChatColor.valueOf(secondaryChannelSwitch) + " from the channel.");
					return true;
				}
			}
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That isn't a player!");
			return true;
		} else if (cmd.getName().equals("ctune")) {
			if(sender instanceof Player){
				if(!(args.length == 0)){
					String selectedChannel = args[0].toLowerCase();
					String selChannelColor = primaryChannelSwitch;
					String playerUUID = ((Player) sender).getUniqueId().toString();
						if(getConfig().getStringList("channels").contains(selectedChannel)){
							if(selectedChannel.equals("global") || selectedChannel.equals("local")) {
								sender.sendMessage(ChatColor.valueOf(errorColor) + "You are automatically tuned into this channel!");
								return true;
							}
							boolean tuneOut = getCurrentChannel().getStringList("ctuned." + selectedChannel).contains(playerUUID);
							String switchedPlayer = ((Player) sender).getUniqueId().toString();
							if(getConfig().getStringList("cban." + selectedChannel).contains(switchedPlayer)) {
								sender.sendMessage(ChatColor.valueOf(errorColor) + "You are banned from this channel!");
								return true;
							}
							if(!(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("all")) && (!tuneOut)) {
								if(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("owner-only")) {
									//NPE?
									if(!getConfig().getString("cowner." + selectedChannel).equals(playerUUID)) {
										sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to tune into this channel!");
										return true;
									}
								} else if(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("trusted-only")) {
									if(!getConfig().getStringList("ctrusted." + selectedChannel).contains(playerUUID) && !getConfig().getString("cowner." + selectedChannel).equals(playerUUID)) {
										sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to tune into this channel!");
										return true;
									}
								} else if(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("none")) {
									sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to tune into this channel!");
									return true;
								}
							}
							if(getConfig().getString("channelcolor." + selectedChannel) != null){
								selChannelColor = getConfig().getString("channelcolor." + selectedChannel);
							}
							if(tuneOut) {
								sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch)  + "You have tuned out of the " + ChatColor.valueOf(selChannelColor) + selectedChannel + ChatColor.valueOf(secondaryChannelSwitch) + " channel!");							
								List<String> s = getCurrentChannel().getStringList("ctuned." + selectedChannel);
								s.remove(playerUUID);
								getCurrentChannel().set("ctuned." + selectedChannel, s);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, untuneMessage, selectedChannel);
								return true;
							}
							if(getConfig().getString("passcodes." + selectedChannel).equals("n/a") || getConfig().getString("passcodes." + selectedChannel).equals("default") || getConfig().getString("passcodes." + selectedChannel).equals("none")){
								sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch)  + "You have tuned into the " + ChatColor.valueOf(selChannelColor) + selectedChannel + ChatColor.valueOf(secondaryChannelSwitch) + " channel!");
								List<String> s = getCurrentChannel().getStringList("ctuned." + selectedChannel);
								s.add(playerUUID);
								getCurrentChannel().set("ctuned." + selectedChannel, s);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, tuneMessage, selectedChannel);
								return true;
							} else {
								if(args.length < 2){
									sender.sendMessage(ChatColor.valueOf(errorColor) + "There is a password to this channel. Use /ctune <channel> <password> to use it!");
									return false;
								}
								if(args[1].equals(getConfig().getString("passcodes." + selectedChannel))){
									sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch)  + "You have tuned into the " + ChatColor.valueOf(selChannelColor) + selectedChannel + ChatColor.valueOf(secondaryChannelSwitch) + " channel!");
									List<String> s = getCurrentChannel().getStringList("ctuned." + selectedChannel);
									s.add(playerUUID);
									getCurrentChannel().set("ctuned." + selectedChannel, s);
									saveCurrentChannel();
									sendEntryExitMessages(sender, cmd, label, args, tuneMessage, selectedChannel);
									return true;
								} else {
									sender.sendMessage(ChatColor.valueOf(errorColor) + "The password was incorrect.");
									return true;
								}
							}	
						}
					sender.sendMessage(ChatColor.valueOf(errorColor) + "That isn't a channel!");
				} else {
					sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify a channel!");
					return false;
				}
			} else {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You aren't a player!");
				return false;
			}
			return false;
		} else if(cmd.getName().equals("csearch")) {
			if(args.length < 2 && !args[0].equals("flags")) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify at least one search term!");
				return false;
			} else if(args[0].equals("flags")) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "The valid flags are: -o to search by owner; -s to search the starting characters; -n to search the entire channel name; and -u to search by user.");
			}
			List<String> argList = Arrays.asList(args);
			List<String> possChannels = getConfig().getStringList("channels");
			//in order to not modify the list we are iterating through, use a duplicate
			List<String> pcDup = new ArrayList<>();
			pcDup = makeEquivalent(possChannels, pcDup);
			//handle -u flag first - easiest to deal with
			if(argList.contains("-u")) {
				int index = argList.indexOf("-u");
				possChannels.clear();
				possChannels.add(getCurrentChannel().getString(getUUIDByName(argList.get(index+1))));
			} 
			if(argList.contains("-n")) {
				int index = argList.indexOf("-n");
				for(String s : pcDup) { 
					if(!s.contains(argList.get(index+1))) {
						possChannels.remove(s);
					}
				}
				pcDup = makeEquivalent(possChannels, pcDup);
			} 
			if(argList.contains("-s")) {
				int index = argList.indexOf("-s");
				for(String s : pcDup) { 
					if(!s.startsWith(argList.get(index+1))) {
						possChannels.remove(s);
					}
				}
				pcDup = makeEquivalent(possChannels, pcDup);
			} 
			if(argList.contains("-o")) {
				int index = argList.indexOf("-o");
				String UUID = getUUIDByName(argList.get(index+1));
				Object[] keyArray = getConfig().getConfigurationSection("cowner").getKeys(false).toArray();
				for(Object s : keyArray) {
					if(!getConfig().getString("cowner." + s).equals(UUID)) {
						possChannels.remove(s);
					}
				}
			}
			if(possChannels.isEmpty()) {
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Your search turned up" + ChatColor.valueOf(primaryChannelSwitch) + " no results" + ChatColor.valueOf(secondaryChannelSwitch) + "!");
				return true;
			}
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Your search turned up the following results: " + ChatColor.valueOf(primaryChannelSwitch) + possChannels.toString().substring(1, possChannels.toString().length() - 1));
			return true;
		} else if(cmd.getName().equals("crestore")) {
			if(args.length > 0) {
				List<String> s = getConfig().getStringList("channels");
				s.add(args[0].toLowerCase());
				getConfig().set("channels", s);
				saveConfig();
				sender.sendMessage(ChatColor.valueOf(errorColor) + "Warning: if the inputted channel did not exist or has been purged, it will be listed in /clist and /csearch, and it will cause issues when a player tries to switch to it. Use /cdel <channel> to undo this.");
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The channel has been restored.");
				return true;
			} else {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must input a channel in order to restore it!");
				return true;
			}
		} else if(cmd.getName().equals("cpurge")) {
			if(args.length > 0) {
				String purgeChannel = args[0].toLowerCase();
				List<String> s = getConfig().getStringList("channels");
				if(purgeChannel.equals(getConfig().getString("default_channel"))) {
					sender.sendMessage(ChatColor.valueOf(errorColor) + "You can't delete the default channel!");
					return true;
				}
				if(s.contains(purgeChannel)) {
					sender.sendMessage(ChatColor.valueOf(errorColor) + "This channel has not been deleted with /cdel yet! It has been done for you: type /cpurge <channel> to purge the channel or /crestore <channel> to undo it.");
					s.remove(purgeChannel);
					getConfig().set("channels", s);
					saveConfig();
					return true;
				}
				getConfig().set("passcodes." + purgeChannel, null);
				getConfig().set("channelcolor." + purgeChannel, null);
				getConfig().set("cowner." + purgeChannel, null);
				getConfig().set("ctrusted." + purgeChannel, null);
				getConfig().set("cban." + purgeChannel, null);
				getConfig().set("cflags." + purgeChannel, null);
				saveConfig();
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The channel has been purged.");
				return true;
			} else {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must input a channel in order to purge it!");
				return true;
			}
		} else if (cmd.getName().equals("creload")) {
			reloadConfig();
			reloadNickname();
			reloadCurrentChannel();
			primaryChannelSwitch = getConfig().getString("colors.PrimaryChannelSwitch");
			secondaryChannelSwitch = getConfig().getString("colors.SecondaryChannelSwitch");
			errorColor = this.getConfig().getString("colors.error");
			nickMaxLength = getConfig().getInt("nick-max-length");
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "ChatChannels has been reloaded!");
			return true;
		} else if (cmd.getName().equals("cinvite")) {
			//mainly copy and paste from ctrust
			//handle miscreants
			if(!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not a player in a channel!");
				return true;
			}
			String currentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			//in English - if the owner of the current channel isn't the player sending the command... 
			if(!getConfig().getString("cowner." + currentChannel).equals(((Player) sender).getUniqueId().toString())) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not the owner of the channel!");
				return true;
			}
			if(args.length == 0) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You must specify which player to invite!");
				return true;
			}
			if(!getConfig().getStringList("invite-only-channels").contains(currentChannel)) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You can only invite people to invite-only channels!");
				return true;
			}
			//now to trust i mean invite people
			List<String> s = getConfig().getStringList("invitees." + currentChannel);
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				if(player.getName().equals(args[0])) {
					if(s.contains(player.getUniqueId().toString())) {
						s.remove(player.getUniqueId().toString());
						sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have " + ChatColor.valueOf(primaryChannelSwitch) + "uninvited " + args[0] + ".");
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have been " + ChatColor.valueOf(primaryChannelSwitch) + "uninvited to " + currentChannel + ".");
					} else {
						s.add(player.getUniqueId().toString());
						sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have " + ChatColor.valueOf(primaryChannelSwitch) + "invited " + args[0]);
						player.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have been " + ChatColor.valueOf(primaryChannelSwitch) + "invited to " + currentChannel + ".");

					}
					getConfig().set("invitees." + currentChannel, s);
					saveConfig();
					return true;
				}
			}
		}
		return false;
	}
	
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
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		
		Player msgSender = event.getPlayer();
		
		if(muteMap.get(msgSender.getUniqueId().toString()) == true){
			msgSender.sendMessage(ChatColor.valueOf(errorColor) + "You are muted!");
			event.setCancelled(true);
			return;
		}
		String prefix = chat.getPlayerPrefix(msgSender);
		String suffix = chat.getPlayerSuffix(msgSender);
		prefix = (prefix.length() > 0) ? prefix + " ": "";
		suffix = (suffix.length() > 0) ? " " + suffix + " ": "";
		String senderName = ((Player) msgSender).getUniqueId().toString();
		//Swing's prefix
		if(senderName.equals("f49d003a-9836-4e76-b76b-68519a1b1fbf")) {
			if(getConfig().getBoolean("give_CCDev_prefix")) {
				prefix = "&6[&eCCDev&6]&f " + prefix;
			}
		}
		String senderCurrentChannel = getCurrentChannel().getString(senderName);
		String message = event.getMessage();
		if(!(getNickname().getString(senderName) == null)){
			String nickname = getNickname().getString(senderName);
			senderName = nickname;
		} else {
			senderName = msgSender.getName();
		}
		//handle special cases - global and local
		if(senderCurrentChannel.equals("global")||senderCurrentChannel.equals("local")){
			if(senderCurrentChannel.equals("global")){
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					boolean isIgnored = false;
					if(ignoreMap.containsKey(player.getUniqueId().toString())){
						String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
						for(String s : ignoreList){
							if(s.equals(((Player) msgSender).getUniqueId().toString())){
								isIgnored = true;
							}
						}
					}
					if(!isIgnored) {
						if(msgSender.hasPermission("chatchannels.message.color")) {
							player.sendMessage(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
						} else {
							player.sendMessage(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
						}		
					}
				}
				System.out.println(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
			} else {
				double sx = msgSender.getLocation().getX();
				double sz = msgSender.getLocation().getZ();
				double range = getConfig().getInt("local_chat_range");
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					double px = Math.abs(player.getLocation().getX());
					double pz = Math.abs(player.getLocation().getX());
					if(Math.abs(sx - px) <= range || Math.abs(sz-pz) <= range || (spyMap.get(getUUIDByName(player.getName())) == true)){
						boolean isIgnored = false;
						if(ignoreMap.containsKey(player.getUniqueId().toString())){
							String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
							for(String s : ignoreList){
								if(s.equals(((Player) msgSender).getUniqueId().toString())){
									isIgnored = true;
								}
							}
						}
						if(!isIgnored) {
							if(msgSender.hasPermission("chatchannels.message.color")) {
								player.sendMessage(ChatColor.YELLOW + "[Local] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
							} else {
								player.sendMessage(ChatColor.YELLOW + "[Local] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
							}		
						}
					}
				}
				System.out.println(ChatColor.YELLOW + "[Local] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
			}
		} else {
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				String playerID = player.getUniqueId().toString();
				String playerCurrentChannel = getCurrentChannel().getString(playerID);
				boolean isIgnored = false;
				if(ignoreMap.containsKey(player.getUniqueId().toString())){
					String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
					isIgnored = false;
					for(String s : ignoreList){
						if(s.equals(((Player) msgSender).getUniqueId().toString())){
							isIgnored = true;
						}
					}
				}
				if(senderCurrentChannel.equalsIgnoreCase("noChannel")){
					msgSender.sendMessage(ChatColor.valueOf(errorColor) + "You aren't in a channel! Use /cg for global chat or choose a channel with /channel");
				} else {
					
					if((playerCurrentChannel.equalsIgnoreCase(senderCurrentChannel) || getCurrentChannel().getStringList("ctuned." + senderCurrentChannel).contains(player.getUniqueId().toString())) && !isIgnored){
						String channelColor = "YELLOW";
						if(getConfig().getString("channelcolor." + senderCurrentChannel) != null){
							channelColor = getConfig().getString("channelcolor." + senderCurrentChannel);
						}
						if(!isIgnored) {
							if(msgSender.hasPermission("chatchannels.message.color")) {
								player.sendMessage(ChatColor.valueOf(channelColor) + "[" + senderCurrentChannel + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
							} else {
								player.sendMessage(ChatColor.valueOf(channelColor) + "[" + senderCurrentChannel + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
							}		
						}
					} else if((spyMap.get(getUUIDByName(player.getName())) == true)){
						String channelColor = "YELLOW";
						if(getConfig().getString("channelcolor." + senderCurrentChannel) != null){
							channelColor = getConfig().getString("channelcolor." + senderCurrentChannel);
						}
						player.sendMessage(ChatColor.YELLOW + "[spy] "+ ChatColor.valueOf(channelColor) + "[" + senderCurrentChannel + "] " + ChatColor.translateAlternateColorCodes('&', senderName) + ": " + message);
					}
				}
			}
			System.out.println(ChatColor.YELLOW + "[" + senderCurrentChannel + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix) + " " + ChatColor.translateAlternateColorCodes('&', senderName) + " " + suffix + " " + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event){
		String loginplayer = event.getPlayer().getUniqueId().toString();
		if(getCurrentChannel().getString(loginplayer) == null){
			getCurrentChannel().set(loginplayer, "red");
			saveCurrentChannel();
		}
		if(!spyMap.containsKey(loginplayer)){
			spyMap.put(loginplayer, false);
		}
		if(!muteMap.containsKey(loginplayer)){
			muteMap.put(loginplayer, false);
		}
	}
	
	public void onAlternateCommandUsed(PlayerCommandPreprocessEvent e){
		String[] cmdArray = e.getMessage().split(" ");
		String baseCmd = cmdArray[0];
		StringBuilder sb = new StringBuilder();
		Player p = e.getPlayer();
		if(baseCmd.equalsIgnoreCase("mute")){
			sb.append("/cmute");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		} else if(baseCmd.equalsIgnoreCase("nick")){
			sb.append("/cnick");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		} else if(baseCmd.equalsIgnoreCase("realname")){
			sb.append("/crealname");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		} else if(baseCmd.equalsIgnoreCase("ignore")){
			sb.append("/cignore");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		}
		sb.delete(0, sb.length() - 1);
	}
	
	@Override
	public void onDisable(){
		plugin = null;
	}
	
	//formerly in onCommand - in an effort to tidy up the code, moved these commands into their own functions
	public boolean doChannelSwitch(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player){
			if(!(args.length == 0)){
				String selectedChannel = args[0].toLowerCase();
				String selChannelColor = primaryChannelSwitch;
					if(getConfig().getStringList("channels").contains(selectedChannel) || getConfig().getStringList("invite-only-channels").contains(selectedChannel)){
						String switchedPlayer = ((Player) sender).getUniqueId().toString();
						if(getConfig().getStringList("cban." + selectedChannel).contains(switchedPlayer)) {
							sender.sendMessage(ChatColor.valueOf(errorColor) + "You have been banned from this channel!");
							return true;
						}
						if(getConfig().getString("channelcolor." + selectedChannel) != null){
							selChannelColor = getConfig().getString("channelcolor." + selectedChannel);
						}
						boolean noPasscode = getConfig().getString("passcodes." + selectedChannel).equals("n/a") || getConfig().getString("passcodes." + selectedChannel).equals("default") || getConfig().getString("passcodes." + selectedChannel).equals("none");
						if(noPasscode && !getConfig().getStringList("invite-only-channels").contains(selectedChannel)){
							sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(switchedPlayer));
							sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch)  + "You have been switched to the " + ChatColor.valueOf(selChannelColor) + selectedChannel + ChatColor.valueOf(secondaryChannelSwitch) + " channel!");
							getCurrentChannel().set(switchedPlayer, selectedChannel);
							saveCurrentChannel();
							sendEntryExitMessages(sender, cmd, label, args, joinMessage, selectedChannel);
							return true;
						} else if(getConfig().getStringList("invite-only-channels").contains(selectedChannel)){
							if(getConfig().getStringList("invitees." + selectedChannel).contains(switchedPlayer) || sender.hasPermission("chatchannels.invite.override")) {
								sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(switchedPlayer));
								sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch)  + "You have been switched to the " + ChatColor.valueOf(selChannelColor) + selectedChannel + ChatColor.valueOf(secondaryChannelSwitch) + " channel!");
								getCurrentChannel().set(switchedPlayer, selectedChannel);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, joinMessage, selectedChannel);
								return true;
							} else {
								sender.sendMessage(ChatColor.valueOf(errorColor) + "That channel doesn't exist!");
								return true;
							}
						} else {
							if(args.length < 2){
								sender.sendMessage(ChatColor.valueOf(errorColor) + "There is a password to this channel. Use /channel <channel> <password> to use it!");
								return false;
							}
							if(args[1].equals(getConfig().getString("passcodes." + selectedChannel))){
								sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(switchedPlayer));
								sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch)  + "You have been switched to the " + ChatColor.valueOf(selChannelColor) + selectedChannel + ChatColor.valueOf(secondaryChannelSwitch) + " channel!");
								getCurrentChannel().set(switchedPlayer, selectedChannel);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, joinMessage, selectedChannel);
								return true;
							} else {
								sender.sendMessage(ChatColor.valueOf(errorColor) + "The password was incorrect.");
								return true;
							}
						}	
					}
				sender.sendMessage(ChatColor.valueOf(errorColor) + "That channel doesn't exist!");
				return true;
			} else {
				sender.sendMessage("You must specify a channel!");
				return false;
			}
		} else {
			sender.sendMessage(ChatColor.valueOf(errorColor) + "You aren't a player!");
			return false;
		}
	}
	
	public boolean doCnick(CommandSender sender, Command cmd, String label, String[] args) {
		boolean nicknameExists = false;
		String pNick = "";
		Object[] UUIDListRaw = getNickname().getKeys(false).toArray();
		StringBuilder UUIDsb = new StringBuilder();
		for(Object o : UUIDListRaw){
			UUIDsb.append(o + " ");
		}
		String[] UUIDList = UUIDsb.toString().split(" ");
		for(String s : UUIDList){
			String plainNick = "";
			String plainNick1 = getNickname().getString(s);
			char[] plainNickArray = args[0].toCharArray();
			char[] plainNickArray1 = plainNick1.toCharArray();
			StringBuilder sb = new StringBuilder();
			StringBuilder sb1 = new StringBuilder();
			for(i = 1; i < plainNickArray.length; i++){
				if(!(plainNickArray[i] == '&')){
					sb.append(plainNickArray[i]);
				} else {
					i++;
				}
			}
			for(i = 1; i < plainNickArray1.length; i++){
				if(!(plainNickArray1[i] == '&')){
					sb1.append(plainNickArray1[i]);
				} else {
					i++;
				}
			}
			plainNick = sb.toString();
			pNick = plainNick;
			plainNick1 = sb1.toString();
			if((getNickname().getString(s) != null) && plainNick.equalsIgnoreCase("~" + plainNick)) nicknameExists = true;
			sb.delete(0, sb.length());
			sb1.delete(0, sb1.length());
			UUIDsb.delete(0, UUIDsb.length());
		}
		if(args.length < 1){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "No nickname defined!");
			return false;
		} else if(!(sender instanceof Player) && args.length > 2){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "You can only set your nickname as a player!");
			return true;
		} else if(nicknameExists){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That nickname exists!");
			return true;
		} else if(pNick.length() > nickMaxLength){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That nickname is too long!");
			return true;
		} else if(!(sender instanceof Player) && args.length == 2){ 
			
			String cmdSenderName = getUUIDByName(args[1]);
			String nick = (args[0].equals("off")) ? args[1] : ("~" + args[0]);
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + args[1] + "'s nickname is now " + ChatColor.translateAlternateColorCodes('&', nick) + ".");
			getNickname().set(cmdSenderName, nick);
			saveNickname();
			return true;
		} else {
			
			String cmdSenderName = ((Player) sender).getUniqueId().toString();
			String nick = (args[0].equals("off")) ? sender.getName() : ("~" + args[0]);
			if(args.length == 2 && sender.hasPermission("chatchannels.nick.other")){
				cmdSenderName = getUUIDByName(args[1]);
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + args[1] + "'s nickname is now " + ChatColor.translateAlternateColorCodes('&', nick) + ".");
			} else {
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Your nickname is now " + ChatColor.translateAlternateColorCodes('&', nick) + ".");
			}
			getNickname().set(cmdSenderName, nick);
			saveNickname();
			return true;
		}
	}
	
	public boolean doCedit(CommandSender sender, Command cmd, String label, String[] args) {
		String selchannel = args[0];
		if(args.length < 2){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "Specify which channel you want to edit and what you want to edit about it!");
			sender.sendMessage(ChatColor.valueOf(errorColor) + "/cedit <channel> <passcode/color/flag>");
			return false;
		}
		if(!getConfig().getStringList("channels").contains(selchannel)){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "That isn't a channel!");
				return true;
		}	
		if(sender instanceof Player) {
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.override.edit"))) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You are not allowed to edit that channel!");
				return true;
			}
		}
		if(args[1].equals("passcode")){
			
			if(args.length < 4){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "Specify a password!");
				sender.sendMessage(ChatColor.valueOf(errorColor) + "/cedit <channel> passcode <oldpassword/default> <newpassword>");
				return false;
				
			}
				String passchannel = selchannel;
				if(getConfig().getString("passcodes." + passchannel).equals(args[2]) && !(getConfig().getString("passcodes." + passchannel).equals("n/a")) && !(args[3].equals("n/a")) && sender.hasPermission("chatchannels.edit.passcode")){
					getConfig().set("passcodes." + passchannel, args[3]);
					saveConfig();
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "You have successfully changed the passcode! The new passcode is: " + ChatColor.valueOf(primaryChannelSwitch) +args[3]);
					return true;
				} else if(getConfig().getString("passcodes." + passchannel).equals("n/a") || !sender.hasPermission("chatchannels.edit.passcode")){
					sender.sendMessage(ChatColor.valueOf(errorColor) + "You cannot change this channel's password!");
					return false;
				} else if(args[3].equals("n/a")){
					sender.sendMessage(ChatColor.valueOf(errorColor) + "You can't make the password unchangeable!");
					return false;
				}
		} else if(args[1].equals("color")){
			if(!sender.hasPermission("chatchannels.edit.color")) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You aren't allowed to change this channel's color!");
				return true;
			}
			if(args.length < 3){
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to specify a color!");
				sender.sendMessage(ChatColor.valueOf(errorColor) + "/cedit <channel> color <color>");
				return false;
			} else {
				String color = args[2].toUpperCase(java.util.Locale.ENGLISH);
				try{
					color = "" + ChatColor.valueOf(args[2].toUpperCase(java.util.Locale.ENGLISH));
				} catch (IllegalArgumentException e){
					sender.sendMessage(ChatColor.valueOf(errorColor) + "That isn't a valid color!");
					return false;
				}
				color = args[2].toUpperCase(java.util.Locale.ENGLISH);
				getConfig().set("channelcolor." + selchannel, color);
				saveConfig();
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "The new color of the channel " + selchannel +  " is " + ChatColor.valueOf(color) + color);
				return true;
			}
		} else if(args[1].equals("flag")) {
			if(args.length < 4) {
				sender.sendMessage(ChatColor.valueOf(errorColor) + "You need to specify a flag and its value!");
				sender.sendMessage(ChatColor.valueOf(errorColor) + "/cedit <channel> flag <flag> <value>");
				return true;
			}
			String[] validFlags = {"join-messages", "tune-messages", "allow-tune"};
			if(Arrays.asList(validFlags).contains(args[2].toLowerCase())) {
				String[] validInput = {"none", "owner-only", "trusted-only", "all"};
				if(!(Arrays.asList(validInput).contains(args[3].toLowerCase()))) {
					sender.sendMessage(ChatColor.valueOf(errorColor) + "That is not a valid value! The valid values are: none, owner-only, trusted-only, all");
					return true;
				} else {
					getConfig().set("cflags." + args[0] + "." + args[2], args[3]);
					saveConfig();
					sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Success! The value of the flag " + ChatColor.valueOf(primaryChannelSwitch) + args[2] + ChatColor.valueOf(secondaryChannelSwitch) + " on " + ChatColor.valueOf(primaryChannelSwitch) + args[0] + ChatColor.valueOf(secondaryChannelSwitch) + " is now " + ChatColor.valueOf(primaryChannelSwitch) + args[3] + ".");
				}
			}
		}
		return true;
	}

	private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
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
							player.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
						} 
					}
					return;	
				} else if(getConfig().getString("cflags." + channel + ".tune-messages").equals("owner-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
					} else {
						return;
					}
				} else if(getConfig().getString("cflags." + channel + ".tune-messages").equals("trusted-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
					} 
					for(String s : getConfig().getStringList("ctrusted." + channel)) {
						Player trusted = Bukkit.getServer().getPlayer(UUID.fromString(s));
						if(trusted != null) {
							trusted.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has tuned " + (isLeaving ? "out of" : "into") + " the channel.");
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
							player.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has " + (isLeaving ? "left" : "joined") + " the channel.");
						} 
					}
					return;	
				} else if(getConfig().getString("cflags." + channel + ".join-messages").equals("owner-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has " + (isLeaving ? "left" : "joined") + " the channel.");
					} else {
						return;
					}
				} else if(getConfig().getString("cflags." + channel + ".join-messages").equals("trusted-only")){
					Player owner = Bukkit.getServer().getPlayer(UUID.fromString(getConfig().getString("cowner." + channel)));
					if(owner != null) {
						owner.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has " + (isLeaving ? "left" : "joined") + " the channel.");
					} 
					for(String s : getConfig().getStringList("ctrusted." + channel)) {
						Player trusted = Bukkit.getServer().getPlayer(UUID.fromString(s));
						if(trusted != null) {
							trusted.sendMessage(ChatColor.valueOf(primaryChannelSwitch) + ChatColor.translateAlternateColorCodes('&', senderName) + ChatColor.valueOf(secondaryChannelSwitch) + " has " + (isLeaving ? "left" : "joined") + " the channel.");
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
