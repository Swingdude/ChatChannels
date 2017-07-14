package com.gmail.nlspector.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class NewChannelCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public NewChannelCommandExecutor(ChatChannel c, String pCS, String sCS, String eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
	}

}
