package com.gmail.nlspector.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelTuneCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelTuneCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player){
			if(!(args.length == 0)){
				String selectedChannel = args[0].toLowerCase();
				String selChannelColor = primary.toString();
				String playerUUID = ((Player) sender).getUniqueId().toString();
					if(getConfig().getStringList("channels").contains(selectedChannel)){
						if(selectedChannel.equals("global") || selectedChannel.equals("local")) {
							sender.sendMessage(error + "You are automatically tuned into this channel!");
							return true;
						}
						boolean tuneOut = getCurrentChannel().getStringList("ctuned." + selectedChannel).contains(playerUUID);
						String switchedPlayer = ((Player) sender).getUniqueId().toString();
						if(getConfig().getStringList("cban." + selectedChannel).contains(switchedPlayer)) {
							sender.sendMessage(error + "You are banned from this channel!");
							return true;
						}
						if(!(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("all")) && (!tuneOut)) {
							if(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("owner-only")) {
								//NPE?
								if(!getConfig().getString("cowner." + selectedChannel).equals(playerUUID)) {
									sender.sendMessage(error + "You are not allowed to tune into this channel!");
									return true;
								}
							} else if(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("trusted-only")) {
								if(!getConfig().getStringList("ctrusted." + selectedChannel).contains(playerUUID) && !getConfig().getString("cowner." + selectedChannel).equals(playerUUID)) {
									sender.sendMessage(error + "You are not allowed to tune into this channel!");
									return true;
								}
							} else if(getConfig().getString("cflags." + selectedChannel + ".allow-tune").equals("none")) {
								sender.sendMessage(error + "You are not allowed to tune into this channel!");
								return true;
							}
						}
						if(getConfig().getString("channelcolor." + selectedChannel) != null){
							selChannelColor = getConfig().getString("channelcolor." + selectedChannel);
						}
						if(tuneOut) {
							sender.sendMessage(secondary  + "You have tuned out of the " + ChatColor.valueOf(selChannelColor) + selectedChannel + secondary + " channel!");							
							List<String> s = getCurrentChannel().getStringList("ctuned." + selectedChannel);
							s.remove(playerUUID);
							getCurrentChannel().set("ctuned." + selectedChannel, s);
							saveCurrentChannel();
							sendEntryExitMessages(sender, cmd, label, args, untuneMessage, selectedChannel);
							return true;
						}
						if(getConfig().getString("passcodes." + selectedChannel).equals("n/a") || getConfig().getString("passcodes." + selectedChannel).equals("default") || getConfig().getString("passcodes." + selectedChannel).equals("none")){
							sender.sendMessage(secondary  + "You have tuned into the " + ChatColor.valueOf(selChannelColor) + selectedChannel + secondary + " channel!");
							List<String> s = getCurrentChannel().getStringList("ctuned." + selectedChannel);
							s.add(playerUUID);
							getCurrentChannel().set("ctuned." + selectedChannel, s);
							saveCurrentChannel();
							sendEntryExitMessages(sender, cmd, label, args, tuneMessage, selectedChannel);
							return true;
						} else {
							if(args.length < 2){
								sender.sendMessage(error + "There is a password to this channel. Use /ctune <channel> <password> to use it!");
								return false;
							}
							if(args[1].equals(getConfig().getString("passcodes." + selectedChannel))){
								sender.sendMessage(secondary  + "You have tuned into the " + ChatColor.valueOf(selChannelColor) + selectedChannel + secondary + " channel!");
								List<String> s = getCurrentChannel().getStringList("ctuned." + selectedChannel);
								s.add(playerUUID);
								getCurrentChannel().set("ctuned." + selectedChannel, s);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, tuneMessage, selectedChannel);
								return true;
							} else {
								sender.sendMessage(error + "The password was incorrect.");
								return true;
							}
						}	
					}
				sender.sendMessage(error + "That isn't a channel!");
			} else {
				sender.sendMessage(error + "You must specify a channel!");
				return false;
			}
		} else {
			sender.sendMessage(error + "You aren't a player!");
			return false;
		}
		return false;
	}

}
