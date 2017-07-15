package com.gmail.nlspector.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.gmail.nlspector.chatchannels.ChatChannel;

public class ChannelSearchCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public ChannelSearchCommandExecutor(ChatChannel c, ChatColor pCS, ChatColor sCS, ChatColor eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length < 2 && !args[0].equals("flags")) {
			sender.sendMessage(error + "You must specify at least one search term!");
			return false;
		} else if(args[0].equals("flags")) {
			sender.sendMessage(error + "The valid flags are: -o to search by owner; -s to search the starting characters; -n to search the entire channel name; and -u to search by user.");
		}
		List<String> argList = Arrays.asList(args);
		List<String> possChannels = getConfig().getStringList("channels");
		//in order to not modify the list we are iterating through, use a duplicate
		List<String> pcDup = new ArrayList<>();
		pcDup = makeEquivalent(possChannels, pcDup);
		//handle -u flag first - easiest to deal with
		if(argList.contains("-u")) {
			int index = argList.indexOf("-u");
			possChannels.clear();
			possChannels.add(getCurrentChannel().getString(getUUIDByName(argList.get(index+1))));
		} 
		if(argList.contains("-n")) {
			int index = argList.indexOf("-n");
			for(String s : pcDup) { 
				if(!s.contains(argList.get(index+1))) {
					possChannels.remove(s);
				}
			}
			pcDup = makeEquivalent(possChannels, pcDup);
		} 
		if(argList.contains("-s")) {
			int index = argList.indexOf("-s");
			for(String s : pcDup) { 
				if(!s.startsWith(argList.get(index+1))) {
					possChannels.remove(s);
				}
			}
			pcDup = makeEquivalent(possChannels, pcDup);
		} 
		if(argList.contains("-o")) {
			int index = argList.indexOf("-o");
			String UUID = getUUIDByName(argList.get(index+1));
			Object[] keyArray = getConfig().getConfigurationSection("cowner").getKeys(false).toArray();
			for(Object s : keyArray) {
				if(!getConfig().getString("cowner." + s).equals(UUID)) {
					possChannels.remove(s);
				}
			}
		}
		if(possChannels.isEmpty()) {
			sender.sendMessage(secondary + "Your search turned up" + primary + " no results" + secondary + "!");
			return true;
		}
		sender.sendMessage(secondary + "Your search turned up the following results: " + primary + possChannels.toString().substring(1, possChannels.toString().length() - 1));
		return true;
	}

}
