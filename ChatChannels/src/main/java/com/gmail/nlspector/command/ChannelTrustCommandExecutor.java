package com.gmail.nlspector.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelTrustCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelTrustCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		//handle miscreants
		if(!(sender instanceof Player)) {
			sender.sendMessage(error + "You are not a player in a channel!");
			return true;
		}
		String currentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
		//in English - if the owner of the current channel isn't the player sending the command... 
		if(!getConfig().getString("cowner." + currentChannel).equals(((Player) sender).getUniqueId().toString()) && !sender.hasPermission("chatchannels.trust.override")) {
			sender.sendMessage(error + "You are not the owner of the channel!");
			return true;
		}
		if(args.length == 0) {
			sender.sendMessage(error + "You must specify which player to trust!");
			return true;
		}
		//now to trust people
		List<String> s = getConfig().getStringList("ctrusted." + currentChannel);
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if(player.getName().equals(args[0])) {
				if(s.contains(player.getUniqueId().toString())) {
					s.remove(player.getUniqueId().toString());
					sender.sendMessage(secondary + "You have " + primary + "untrusted " + args[0] + ".");
					player.sendMessage(secondary + "You have been " + primary + "untrusted on " + currentChannel + ".");
				} else {
					s.add(player.getUniqueId().toString());
					sender.sendMessage(secondary + "You have " + primary + "trusted " + args[0]);
					player.sendMessage(secondary + "You have been " + primary + "trusted on " + currentChannel + ".");

				}
				getConfig().set("ctrusted." + currentChannel, s);
				saveConfig();
				return true;
			}
		}
		return false;
	}

}
