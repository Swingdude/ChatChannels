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

	public ChannelBanCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(cmd.getName().equals("cban")) {
			//deal with the permissions
			if(args.length == 0) {
				sender.sendMessage(error + "You must specify a player to ban!");
				return false;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(error + "You must be a player to ban someone from a channel!");
				return false;
			}
			String selchannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			if(selchannel.equals(getConfig().getString("default_channel"))) {
				sender.sendMessage(error + "You can't ban someone from the default channel!");
				return true;
			}
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.ban.override"))) {
				sender.sendMessage(error + "You are not allowed to ban someone from the channel!");
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
						player.sendMessage(secondary + "You were banned from the channel: " + primary + selchannel + secondary + "! Moving you to: " + primary + defaultChannel);		
						getCurrentChannel().set(player.getUniqueId().toString(), defaultChannel);
						saveCurrentChannel();
					} else if (getCurrentChannel().getStringList("ctuned." + selchannel).contains(player.getUniqueId().toString())){
						sendEntryExitMessages(sender, cmd, label, args, untuneMessage, getCurrentChannel().getString(player.getUniqueId().toString()));
						player.sendMessage(secondary + "You were banned from the channel: " + primary + selchannel + secondary + "! Tuning you out...");
						List<String> s2 = getConfig().getStringList("ctuned." + selchannel);
						s2.remove(player.getUniqueId().toString());
						getCurrentChannel().set("ctuned." + selchannel, s2);
						saveCurrentChannel();
					} else {
						player.sendMessage(secondary + "You were banned from the channel: " + primary + selchannel + secondary + "!");
					}
					sender.sendMessage(secondary + "You have banned " + primary + args[0] + secondary + " from the channel.");
					
					return true;
				}
			}
			sender.sendMessage(error + "That isn't a player!");
			return true;
			
		} else if(cmd.getName().equals("cpardon")) {
			//deal with the permissions
			if(args.length == 0) {
				sender.sendMessage(error + "You must specify a player to pardon!");
				return false;
			}
			if(!(sender instanceof Player)){
				sender.sendMessage(error + "You must be a player to pardon someone from a channel!");
				return false;
			}
			String selchannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
			if(selchannel == getConfig().getString("default_channel")) {
				sender.sendMessage(error + "You can't pardon someone from the default channel!");
				return true;
			}
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.ban.override"))) {
				sender.sendMessage(error + "You are not allowed to pardon someone from this channel!");
				return true;
			}
			for(Player player: Bukkit.getServer().getOnlinePlayers()){
				if(player.getName().equals(args[0])){
					List<String> s = getConfig().getStringList("cban." + selchannel);
					s.remove(player.getUniqueId().toString());
					getConfig().set("cban." + selchannel, s);
					saveConfig();
					player.sendMessage(secondary + "You were unbanned from the channel: " + primary + selchannel + secondary + "!");
					sender.sendMessage(secondary + "You have unbanned " + primary + args[0] + secondary + " from the channel.");
					return true;
				}
			}
			sender.sendMessage(error + "That isn't a player!");
			return true;
		} 
		return false;
	}

}
