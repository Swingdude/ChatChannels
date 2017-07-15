package com.gmail.nlspector.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelInviteCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelInviteCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		//mainly copy and paste from ctrust
		//handle miscreants
		if(!(sender instanceof Player)) {
			sender.sendMessage(error + "You are not a player in a channel!");
			return true;
		}
		String currentChannel = getCurrentChannel().getString(((Player) sender).getUniqueId().toString());
		//in English - if the owner of the current channel isn't the player sending the command... 
		if(!getConfig().getString("cowner." + currentChannel).equals(((Player) sender).getUniqueId().toString()) && !sender.hasPermission("chatchannels.invite.override")) {
			sender.sendMessage(error + "You do not have permission to invite people to this channel!!");
			return true;
		}
		if(args.length == 0) {
			sender.sendMessage(error + "You must specify which player to invite!");
			return true;
		}
		if(!getConfig().getStringList("invite-only-channels").contains(currentChannel)) {
			sender.sendMessage(error + "You can only invite people to invite-only channels!");
			return true;
		}
		//now to trust i mean invite people
		List<String> s = getConfig().getStringList("invitees." + currentChannel);
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			if(player.getName().equals(args[0])) {
				if(s.contains(player.getUniqueId().toString())) {
					s.remove(player.getUniqueId().toString());
					sender.sendMessage(secondary + "You have " + primary + "uninvited " + args[0] + ".");
					player.sendMessage(secondary + "You have been " + primary + "uninvited to " + currentChannel + ".");
				} else {
					s.add(player.getUniqueId().toString());
					sender.sendMessage(secondary + "You have " + primary + "invited " + args[0]);
					player.sendMessage(secondary + "You have been " + primary + "invited to " + currentChannel + ".");

				}
				getConfig().set("invitees." + currentChannel, s);
				saveConfig();
				return true;
			}
		}
		return false;
	}

}
