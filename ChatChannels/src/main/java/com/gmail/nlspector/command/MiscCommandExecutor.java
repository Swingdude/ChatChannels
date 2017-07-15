package com.gmail.nlspector.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

import net.milkbowl.vault.chat.Chat;

public class MiscCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public MiscCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("cg")){
			String senderUUID = getUUIDByName(sender.getName());
			String senderName = "";
			if(!(getNickname().getString(senderUUID) == null)){
				String nickname = getNickname().getString(senderName);
				senderName = nickname;
			} else {
				senderName = sender.getName();
			}
			if(args.length < 1){
				sender.sendMessage(error + "You need to type a message!");
			} else if(plugin.muteMap.get(senderUUID) == true){
				sender.sendMessage(error + "You are muted!");
				return true;
			} else {
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < args.length; i++){
					sb.append(args[i] + " ");
				}
				String message = sb.toString();
				Chat chat = plugin.chat;
				String prefix = chat.getPlayerPrefix((Player) sender);
				String suffix = chat.getPlayerSuffix((Player) sender);
				prefix = (prefix.length() > 0) ? prefix + " ": "";
				suffix = (suffix.length() > 0) ? " " + suffix + " ": "";
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					boolean isIgnored = false;

					if(plugin.ignoreMap.containsKey(senderUUID)){
						String[] ignoreList = plugin.ignoreMap.get(((Player) player).getUniqueId().toString());
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
		} else if(cmd.getName().equals("clist")){
			String channellist = getConfig().getStringList("channels").toString();
			sender.sendMessage(secondary + "The available channels are: " + primary + channellist.substring(1, channellist.length()-1));
			return true;
		} else if(cmd.getName().equals("cplayers")){
			String senderCurrentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			if(senderCurrentChannel.equals("noChannel")){
				sender.sendMessage(error + "You need to be in a channel to list the people in it.");
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
				sender.sendMessage(secondary + "The players in your channel are: " + primary + cplayerlist.toString());
				return true;
			} else {
				sender.sendMessage(error + "You can't list players in your channel if you are not a player yourself!");
				return false;
			}
		} else if(cmd.getName().equals("cspy")){
			if(sender instanceof Player){
				String UUID = ((Player) sender).getUniqueId().toString();
				boolean isSpying = plugin.spyMap.get(UUID);
				plugin.spyMap.put(UUID, !isSpying);
				String spyModeStatus = !isSpying ? "on" : "off";
				sender.sendMessage(secondary + "You have turned spy mode " + primary + spyModeStatus + ".");
				return true;
			} else {
				sender.sendMessage(error + "You already have /cspy, Console user!");
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
					sender.sendMessage(error + "You must specify a valid player!");
					return false;
				}
				String UUID = getUUIDByName(args[0]);
				boolean isMuted = plugin.muteMap.get(UUID);
				plugin.muteMap.put(UUID, !isMuted);
				String muteStatus = !isMuted ? "muted" : "unmuted";
				sender.sendMessage(secondary + "You have " + primary + muteStatus  + " " + args[0] + ".");
				return true;
			} else {
				sender.sendMessage(error + "You need to specify a player!");
				return false;
			}
		} else if(cmd.getName().equals("crealname")){
			if(args.length < 1){
				sender.sendMessage(error + "You need to specify a player!");
				return false;
			}
			for(Player p : Bukkit.getServer().getOnlinePlayers()){
				String plainNick = getNickname().getString(p.getUniqueId().toString());
				char[] plainNickArray = plainNick.toCharArray();
				StringBuilder sb = new StringBuilder();
				for(int i = 1; i < plainNickArray.length; i++){
					if(!(plainNickArray[i] == '&')){
						sb.append(plainNickArray[i]);
					} else {
						i++;
					}
					plainNick = sb.toString();
				}
				if(plainNick.equalsIgnoreCase(args[0])){
					sender.sendMessage(primary + p.getName() + secondary + " has the nickname: " + getNickname().getString(p.getUniqueId().toString()));
					return true;
				}
			}
			sender.sendMessage(error + "That player is not on or that is not a real nickname.");
		} else if(cmd.getName().equals("cignore")){
			if(args.length < 1) sender.sendMessage(error + "You need to specify who you want to ignore!");
			else if(!(sender instanceof Player)) sender.sendMessage(error + "You need to be a player to ignore someone!");
			else {
				String[] ignoreList;
				if(!plugin.ignoreMap.containsKey(getUUIDByName(sender.getName()))) {
					ignoreList = new String[1];
					ignoreList[0] = getUUIDByName(args[0]);
					plugin.ignoreMap.put(getUUIDByName(sender.getName()), ignoreList);
					sender.sendMessage(secondary + "You have ignored " + primary + args[0]);
					return true;
				}
				else{
					ignoreList = new String[plugin.ignoreMap.get(getUUIDByName(sender.getName())).length + 1];
					for(int i = 0; i < ignoreList.length - 1; i++){
						ignoreList[i] = plugin.ignoreMap.get(getUUIDByName(sender.getName()))[i];
					}
					for(int i = 0; i < ignoreList.length - 1; i++){
						if(ignoreList[i].equals(getUUIDByName(args[0]))){
							ignoreList[i] = ignoreList[ignoreList.length - 1];
							ignoreList[ignoreList.length - 1] = null;
							plugin.ignoreMap.put(getUUIDByName(sender.getName()), ignoreList);
							sender.sendMessage(secondary + "You have unignored " + primary + args[0]);
							//garbage collection :D
							if(ignoreList[0] == null) plugin.ignoreMap.remove(getUUIDByName(sender.getName()));
							return true;
						} 
					} 
					ignoreList[ignoreList.length] = getUUIDByName(args[0]);
					sender.sendMessage(secondary + "You have ignored " + primary + args[0]);
				}
				plugin.ignoreMap.put(getUUIDByName(sender.getName()), ignoreList);
				return true;
			}
		} else if(cmd.getName().equals("cowner")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage(error + "You are not a player in a channel!");
				return true;
			}
			String currentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			String owner = getConfig().getString("cowner." + currentChannel);
			owner = (owner.equals("no_owner")) ? owner : (Bukkit.getPlayer(UUID.fromString(owner)).getName());
			sender.sendMessage(secondary + "The owner of this channel is " + primary + owner + ".");
			return true;
		} else if(cmd.getName().equals("crestore")) {
			if(args.length > 0) {
				List<String> s = getConfig().getStringList("channels");
				s.add(args[0].toLowerCase());
				getConfig().set("channels", s);
				saveConfig();
				sender.sendMessage(error + "Warning: if the inputted channel did not exist or has been purged, it will be listed in /clist and /csearch, and it will cause issues when a player tries to switch to it. Use /cdel <channel> to undo this.");
				sender.sendMessage(secondary + "The channel has been restored.");
				return true;
			} else {
				sender.sendMessage(error + "You must input a channel in order to restore it!");
				return true;
			}
		} else if(cmd.getName().equals("cpurge")) {
			if(args.length > 0) {
				String purgeChannel = args[0].toLowerCase();
				List<String> s = getConfig().getStringList("channels");
				if(purgeChannel.equals(getConfig().getString("default_channel"))) {
					sender.sendMessage(error + "You can't delete the default channel!");
					return true;
				}
				if(s.contains(purgeChannel)) {
					sender.sendMessage(error + "This channel has not been deleted with /cdel yet! It has been done for you: type /cpurge <channel> to purge the channel or /crestore <channel> to undo it.");
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
				sender.sendMessage(secondary + "The channel has been purged.");
				return true;
			} else {
				sender.sendMessage(error + "You must input a channel in order to purge it!");
				return true;
			}
		} else if (cmd.getName().equals("creload")) {
			plugin.reloadConfig();
			plugin.reloadNickname();
			plugin.reloadCurrentChannel();
			primary = ChatColor.valueOf(getConfig().getString("colors.PrimaryChannelSwitch"));
			secondary = ChatColor.valueOf(getConfig().getString("colors.SecondaryChannelSwitch"));
			error = ChatColor.valueOf(this.getConfig().getString("colors.error"));
			nickMaxLength = getConfig().getInt("nick-max-length");
			sender.sendMessage(secondary + "ChatChannels has been reloaded!");
			return true;
		} else if(cmd.getName().equalsIgnoreCase("cmsg")){
			String senderUUID = getUUIDByName(sender.getName());
			String senderName = "";
			Player player;
			
			try {
				player = Bukkit.getPlayer(UUID.fromString(getUUIDByName(args[0])));
			} catch (IllegalArgumentException e) {
				sender.sendMessage(error + "That isn't a player!");
				return true;
			}
			
			if(!(getNickname().getString(senderUUID) == null)){
				String nickname = getNickname().getString(senderName);
				senderName = nickname;
			} else {
				senderName = sender.getName();
			}
			if(args.length < 2){
				sender.sendMessage(error + "You need to type a message!");
				return false;
			} else if(player == null) {
				sender.sendMessage(error + "That isn't a player!");
				return true;
			} else {
				StringBuilder sb = new StringBuilder();
				for(int i = 1; i < args.length; i++){
					sb.append(args[i] + " ");
				}
				String message = sb.toString();
				boolean isIgnored = false;
				if(plugin.ignoreMap.containsKey(senderUUID)){
					String[] ignoreList = plugin.ignoreMap.get(((Player) player).getUniqueId().toString());
					for(String s : ignoreList){
						if(s.equals(getUUIDByName(sender.getName()))){
							isIgnored = true;
						}
					}
				}
				
				if(!isIgnored) {
					String playerName = "";
					if(!(getNickname().getString(player.getUniqueId().toString()) == null)){
						String nickname = getNickname().getString(senderName);
						playerName = nickname;
					} else {
						playerName = sender.getName();
					}
					if(sender.hasPermission("chatchannels.message.color")) {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " to " + primary + "you: " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', message));
						sender.sendMessage(primary + "You " + secondary + "to " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', playerName) + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
					} else {
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', senderName) + secondary + " to " + primary + "you: " + message);
						sender.sendMessage(primary + "You" + secondary + " to " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', playerName) + ChatColor.WHITE + ": " + message);
					}
				}
				return true;
			}
		}
		return false;
	}
	

}
