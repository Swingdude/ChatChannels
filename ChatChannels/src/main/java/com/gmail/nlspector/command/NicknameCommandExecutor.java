package com.gmail.nlspector.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.nlspector.chatchannels.ChatChannel;

public class NicknameCommandExecutor extends ChatChannelCommandExecutor implements CommandExecutor{

	public NicknameCommandExecutor(ChatChannel c, String pCS, String sCS, String eC, int nickMaxLen) {
		super(c, pCS, sCS, eC, nickMaxLen);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		boolean nicknameExists = false;
		String pNick = "";
		Object[] UUIDListRaw = getNickname().getKeys(false).toArray();
		StringBuilder UUIDsb = new StringBuilder();
		for(Object o : UUIDListRaw){
			UUIDsb.append(o + " ");
		}
		String[] UUIDList = UUIDsb.toString().split(" ");
		for(String s : UUIDList){
			String plainNick = "";
			String plainNick1 = getNickname().getString(s);
			char[] plainNickArray = args[0].toCharArray();
			char[] plainNickArray1 = plainNick1.toCharArray();
			StringBuilder sb = new StringBuilder();
			StringBuilder sb1 = new StringBuilder();
			for(int i = 1; i < plainNickArray.length; i++){
				if(!(plainNickArray[i] == '&')){
					sb.append(plainNickArray[i]);
				} else {
					i++;
				}
			}
			for(int i = 1; i < plainNickArray1.length; i++){
				if(!(plainNickArray1[i] == '&')){
					sb1.append(plainNickArray1[i]);
				} else {
					i++;
				}
			}
			plainNick = sb.toString();
			pNick = plainNick;
			plainNick1 = sb1.toString();
			if((getNickname().getString(s) != null) && plainNick.equalsIgnoreCase("~" + plainNick)) nicknameExists = true;
			sb.delete(0, sb.length());
			sb1.delete(0, sb1.length());
			UUIDsb.delete(0, UUIDsb.length());
		}
		if(args.length < 1){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "No nickname defined!");
			return false;
		} else if(!(sender instanceof Player) && args.length < 2){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "You can only set your nickname as a player!");
			return true;
		} else if(nicknameExists){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That nickname exists!");
			return true;
		} else if(pNick.length() > nickMaxLength){
			sender.sendMessage(ChatColor.valueOf(errorColor) + "That nickname is too long!");
			return true;
		} else if(!(sender instanceof Player) && args.length == 2){ 
			
			String cmdSenderName = getUUIDByName(args[1]);
			String nick = (args[0].equals("off")) ? args[1] : ("~" + args[0]);
			sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + args[1] + "'s nickname is now " + ChatColor.translateAlternateColorCodes('&', nick) + ".");
			getNickname().set(cmdSenderName, nick);
			saveNickname();
			return true;
		} else {
			
			String cmdSenderName = ((Player) sender).getUniqueId().toString();
			String nick = (args[0].equals("off")) ? sender.getName() : ("~" + args[0]);
			if(args.length == 2 && sender.hasPermission("chatchannels.nick.other")){
				cmdSenderName = getUUIDByName(args[1]);
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + args[1] + "'s nickname is now " + ChatColor.translateAlternateColorCodes('&', nick) + ".");
			} else {
				sender.sendMessage(ChatColor.valueOf(secondaryChannelSwitch) + "Your nickname is now " + ChatColor.translateAlternateColorCodes('&', nick) + ".");
			}
			getNickname().set(cmdSenderName, nick);
			saveNickname();
			return true;
		}
	}

}
