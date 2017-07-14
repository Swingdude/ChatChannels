package com.gmail.nlspector.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelBanCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelBanCommandExecutor(ChatChannel c, String pCS, String sCS, String eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equals("cban")) {
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
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.ban.override"))) {
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
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.ban.override"))) {
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
		} 
		return false;
	}

}
