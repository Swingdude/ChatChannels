package com.gmail.nlspector.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelSwitchCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor {

	public ChannelSwitchCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player){
			if(!(args.length == 0)){
				String selectedChannel = args[0].toLowerCase();
				String selChannelColor = primary.toString();
					if(getConfig().getStringList("channels").contains(selectedChannel) || getConfig().getStringList("invite-only-channels").contains(selectedChannel)){
						String switchedPlayer = ((Player) sender).getUniqueId().toString();
						if(getConfig().getStringList("cban." + selectedChannel).contains(switchedPlayer)) {
							sender.sendMessage(error + "You have been banned from this channel!");
							return true;
						}
						if(getConfig().getString("channelcolor." + selectedChannel) != null){
							selChannelColor = getConfig().getString("channelcolor." + selectedChannel);
						}
						boolean noPasscode = getConfig().getString("passcodes." + selectedChannel).equals("n/a") || getConfig().getString("passcodes." + selectedChannel).equals("default") || getConfig().getString("passcodes." + selectedChannel).equals("none");
						if(noPasscode && !getConfig().getStringList("invite-only-channels").contains(selectedChannel)){
							sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(switchedPlayer));
							sender.sendMessage(secondary  + "You have been switched to the " + ChatColor.valueOf(selChannelColor) + selectedChannel + secondary + " channel!");
							getCurrentChannel().set(switchedPlayer, selectedChannel);
							saveCurrentChannel();
							sendEntryExitMessages(sender, cmd, label, args, joinMessage, selectedChannel);
							return true;
						} else if(getConfig().getStringList("invite-only-channels").contains(selectedChannel)){
							if(getConfig().getStringList("invitees." + selectedChannel).contains(switchedPlayer) || sender.hasPermission("chatchannels.invite.override")) {
								sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(switchedPlayer));
								sender.sendMessage(secondary  + "You have been switched to the " + ChatColor.valueOf(selChannelColor) + selectedChannel + secondary + " channel!");
								getCurrentChannel().set(switchedPlayer, selectedChannel);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, joinMessage, selectedChannel);
								return true;
							} else {
								sender.sendMessage(error + "That channel doesn't exist!");
								return true;
							}
						} else {
							if(args.length < 2){
								sender.sendMessage(error + "There is a password to this channel. Use /channel <channel> <password> to use it!");
								return false;
							}
							if(args[1].equals(getConfig().getString("passcodes." + selectedChannel))){
								sendEntryExitMessages(sender, cmd, label, args, leaveMessage, getCurrentChannel().getString(switchedPlayer));
								sender.sendMessage(secondary  + "You have been switched to the " + ChatColor.valueOf(selChannelColor) + selectedChannel + secondary + " channel!");
								getCurrentChannel().set(switchedPlayer, selectedChannel);
								saveCurrentChannel();
								sendEntryExitMessages(sender, cmd, label, args, joinMessage, selectedChannel);
								return true;
							} else {
								sender.sendMessage(error + "The password was incorrect.");
								return true;
							}
						}	
					}
				sender.sendMessage(error + "That channel doesn't exist!");
				return true;
			} else {
				sender.sendMessage("You must specify a channel!");
				return false;
			}
		} else {
			sender.sendMessage(error + "You aren't a player!");
			return false;
		}
	}

}
