package com.gmail.nlspector.command;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelEditCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelEditCommandExecutor(ChatChannel c, String pCS, String sCS, String eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
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
			if(!(getConfig().getString("cowner." + selchannel).equals(((Player) sender).getUniqueId().toString()) || isTrusted(selchannel, (Player) sender) || sender.hasPermission("chatchannels.edit.override"))) {
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

}
