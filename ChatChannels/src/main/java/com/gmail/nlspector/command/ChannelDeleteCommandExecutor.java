package com.gmail.nlspector.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelDeleteCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelDeleteCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//copy and paste mostly from ctrust
		//handle miscreants
		if(!(sender instanceof Player)) {
			sender.sendMessage(error + "You are not a player in a channel!");
			return true;
		}
		
		String selchannel = args[0];
		if(!(getConfig().getStringList("channels").contains(selchannel) && getConfig().getStringList("invite-only-channels").contains(selchannel))) {
			sender.sendMessage(error + "That channel doesn't exist!");
			return true;
		}
		//in English - if the owner of the current channel isn't the player sending the command nor has permission to override... 
		if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || sender.hasPermission("chatchannels.delete.override"))) {
			sender.sendMessage(error + "You are not allowed to delete this channel!");
			return true;
		}
		if(selchannel.equals(getConfig().getString("default_channel"))) {
			sender.sendMessage(error + "You can't delete the default channel!");
			return true;
		}
		if(args.length == 0) {
			sender.sendMessage(error + "You must specify which channel to delete!");
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
				player.sendMessage(secondary + "The channel you were on was deleted! Moving you to: " + primary + defaultChannel);
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
				sender.sendMessage(secondary + "You have purged the channel " + primary + args[0] + ".");
				return true;
			} else if(args[1].equalsIgnoreCase("purge") && !sender.hasPermission("chatchannels.delete.purge")) {
				sender.sendMessage(error + "You don't have permission to purge this channel.");
			}
		}
		sender.sendMessage(secondary + "You have deleted the channel " + primary + args[0] + ".");
		return true;
	}

}
