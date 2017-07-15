package com.gmail.nlspector.chatchannels;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.nlspector.command.ChannelBanCommandExecutor;
import com.gmail.nlspector.command.ChannelDeleteCommandExecutor;
import com.gmail.nlspector.command.ChannelEditCommandExecutor;
import com.gmail.nlspector.command.ChannelInviteCommandExecutor;
import com.gmail.nlspector.command.ChannelSearchCommandExecutor;
import com.gmail.nlspector.command.ChannelSwitchCommandExecutor;
import com.gmail.nlspector.command.ChannelTrustCommandExecutor;
import com.gmail.nlspector.command.ChannelTuneCommandExecutor;
import com.gmail.nlspector.command.MiscCommandExecutor;
import com.gmail.nlspector.command.NewChannelCommandExecutor;
import com.gmail.nlspector.command.NicknameCommandExecutor;

import net.milkbowl.vault.chat.Chat;

public class ChatChannel extends JavaPlugin implements Listener {

	ChatColor primary;
	ChatColor secondary;
	ChatColor error;
	int i = 1;
	int tuneMessage = 0;
	int joinMessage = 1;
	int leaveMessage = 2;
	int untuneMessage = 3;
	int nickMaxLength;
	public static ChatChannel plugin;
	public Chat chat;
	public Map<String, Boolean> spyMap = new HashMap<>();
	public Map<String, Boolean> muteMap = new HashMap<>();
	public Map<String, String[]> ignoreMap = new HashMap<>();
	
	@Override
	public void onEnable(){
		plugin = this;
		saveDefaultConfig();
		getConfig();
		getServer().getPluginManager().registerEvents(this, this);
		primary = ChatColor.valueOf(getConfig().getString("colors.primary"));
		secondary = ChatColor.valueOf(getConfig().getString("colors.secondary"));
		error = ChatColor.valueOf(this.getConfig().getString("colors.error"));
		nickMaxLength = getConfig().getInt("nick-max-length");
		saveDefaultNickname();
		saveDefaultCurrentChannel();
		setupChat();
		registerCommands();
		boolean errorsExist = false;
		//Notify the console user if the config is messed up.
		if(!getConfig().getStringList("channels").contains(getConfig().getString("default_channel"))) {
			errorsExist = true;
			getLogger().severe("Error in the config file - the default channel is set to a channel that doesn't exist! Are all the letters lowercase?");
		}
		
		if(errorsExist) getLogger().info("ChatChannels v0.5.0-BETA is enabled with errors!");
		else getLogger().info("ChatChannels v0.5.0-BETA is enabled!");
		
	}

	private boolean setupChat()
    {
        RegisteredServiceProvider<Chat> chatProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }

        return (chat != null);
    }
	
	public void registerCommands() {
		this.getCommand("channel").setExecutor(new ChannelSwitchCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("cnick").setExecutor(new NicknameCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("cedit").setExecutor(new ChannelEditCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("newchannel").setExecutor(new NewChannelCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("cdel").setExecutor(new ChannelDeleteCommandExecutor(this, primary, secondary, error, nickMaxLength));
		ChannelBanCommandExecutor cbce = new ChannelBanCommandExecutor(this, primary, secondary, error, nickMaxLength);
		this.getCommand("cban").setExecutor(cbce);
		this.getCommand("cpardon").setExecutor(cbce);
		this.getCommand("ctune").setExecutor(new ChannelTuneCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("csearch").setExecutor(new ChannelSearchCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("cinvite").setExecutor(new ChannelInviteCommandExecutor(this, primary, secondary, error, nickMaxLength));
		this.getCommand("ctrust").setExecutor(new ChannelTrustCommandExecutor(this, primary, secondary, error, nickMaxLength));
		String[] miscCommands = {"clist", "cg", "cmute", "crealname", "cignore", "cowner", "crestore", "creload", "cpurge", "cspy", "cplayers", "cmsg"};
		MiscCommandExecutor mce = new MiscCommandExecutor(this, primary, secondary, error, nickMaxLength);
		for(String command : miscCommands) {
			this.getCommand(command).setExecutor(mce);
		}
	}
	
	//get custom configs
	 File nicknameFile;
	 FileConfiguration nickname;
	//start custom config spam
	public void reloadNickname(){
		if(nicknameFile == null){
			nicknameFile = new File(getDataFolder(), "nicknames.yml");
		}
		nickname = YamlConfiguration.loadConfiguration(nicknameFile);
	}
	
	public FileConfiguration getNickname() {
		if(nickname == null){
			reloadNickname();
		}
		
		return nickname;
	} 
	
	public void saveNickname(){
		if (nickname == null || nicknameFile == null) {
	    return;
	  }
		try {
	    getNickname().save(nicknameFile);
	  } catch (IOException ex) {
	    getLogger().log(Level.SEVERE, "Could not save config to " + nicknameFile, ex);
	  }
		
		
	}
	
	public void saveDefaultNickname() {
	  if (nicknameFile == null) {
	    nicknameFile = new File(getDataFolder(), "nicknames.yml");
	  }
	  if (!nicknameFile.exists()) {      
	     saveResource("nicknames.yml", false);
	  }
	}

	 File currentchannelFile;
	 FileConfiguration currentchannel;
	
	public void reloadCurrentChannel(){
		if(currentchannelFile == null){
			currentchannelFile = new File(getDataFolder(), "currentchannels.yml");
		}
		currentchannel = YamlConfiguration.loadConfiguration(currentchannelFile);
	}
	
	public FileConfiguration getCurrentChannel() {
		if(currentchannel == null){
			reloadCurrentChannel();
		}
		return currentchannel;
	} 
	
	public void saveCurrentChannel(){
		if (currentchannel == null || currentchannelFile == null) {
	    return;
	  }
		try {
	    getCurrentChannel().save(currentchannelFile);
	  } catch (IOException ex) {
	    getLogger().log(Level.SEVERE, "Could not save config to " + currentchannelFile, ex);
	  }
		
		
	}
	
	public void saveDefaultCurrentChannel() {
	  if (currentchannelFile == null) {
	    currentchannelFile = new File(getDataFolder(), "currentchannels.yml");
	  }
	  if (!currentchannelFile.exists()) {      
	     saveResource("currentchannels.yml", false);
	  }
		
		
	}
	//end custom config spam
	
	//This function... is very long.
	//And uncommented. I'll be working on that.
	//And the rest of the code as well.
	
	
	public String getUUIDByName(String name){
		String UUID = "Error";
		for(Player player : Bukkit.getServer().getOnlinePlayers()){
			UUID = player.getName().equals(name) ? player.getUniqueId().toString() : "Error";
			if(!UUID.equals("Error")){
				return UUID;
			}
		}
		return "Error";
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent event){
		
		Player msgSender = event.getPlayer();
		
		if(muteMap.get(msgSender.getUniqueId().toString()) == true){
			msgSender.sendMessage(error + "You are muted!");
			event.setCancelled(true);
			return;
		}
		String prefix = chat.getPlayerPrefix(msgSender);
		String suffix = chat.getPlayerSuffix(msgSender);
		prefix = (prefix.length() > 0) ? prefix + " ": "";
		suffix = (suffix.length() > 0) ? " " + suffix + " ": "";
		String senderName = ((Player) msgSender).getUniqueId().toString();
		//Swing's prefix
		if(senderName.equals("f49d003a-9836-4e76-b76b-68519a1b1fbf")) {
			if(getConfig().getBoolean("give_CCDev_prefix")) {
				prefix = "&6[&eCCDev&6]&f " + prefix;
			}
		}
		String senderCurrentChannel = getCurrentChannel().getString(senderName);
		String message = event.getMessage();
		if(!(getNickname().getString(senderName) == null)){
			String nickname = getNickname().getString(senderName);
			senderName = nickname;
		} else {
			senderName = msgSender.getName();
		}
		//handle special cases - global and local
		if(senderCurrentChannel.equals("global")||senderCurrentChannel.equals("local")){
			if(senderCurrentChannel.equals("global")){
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					boolean isIgnored = false;
					if(ignoreMap.containsKey(player.getUniqueId().toString())){
						String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
						for(String s : ignoreList){
							if(s.equals(((Player) msgSender).getUniqueId().toString())){
								isIgnored = true;
							}
						}
					}
					if(!isIgnored) {
						if(msgSender.hasPermission("chatchannels.message.color")) {
							player.sendMessage(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
						} else {
							player.sendMessage(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
						}		
					}
				}
				System.out.println(ChatColor.GREEN + "[Global] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix) + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
			} else {
				double sx = msgSender.getLocation().getX();
				double sz = msgSender.getLocation().getZ();
				double range = getConfig().getInt("local_chat_range");
				for(Player player : Bukkit.getServer().getOnlinePlayers()){
					double px = Math.abs(player.getLocation().getX());
					double pz = Math.abs(player.getLocation().getX());
					if(Math.abs(sx - px) <= range || Math.abs(sz-pz) <= range || (spyMap.get(getUUIDByName(player.getName())) == true)){
						boolean isIgnored = false;
						if(ignoreMap.containsKey(player.getUniqueId().toString())){
							String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
							for(String s : ignoreList){
								if(s.equals(((Player) msgSender).getUniqueId().toString())){
									isIgnored = true;
								}
							}
						}
						if(!isIgnored) {
							if(msgSender.hasPermission("chatchannels.message.color")) {
								player.sendMessage(ChatColor.YELLOW + "[Local] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
							} else {
								player.sendMessage(ChatColor.YELLOW + "[Local] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
							}		
						}
					}
				}
				System.out.println(ChatColor.YELLOW + "[Local] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
			}
		} else {
			for(Player player : Bukkit.getServer().getOnlinePlayers()){
				String playerID = player.getUniqueId().toString();
				String playerCurrentChannel = getCurrentChannel().getString(playerID);
				boolean isIgnored = false;
				if(ignoreMap.containsKey(player.getUniqueId().toString())){
					String[] ignoreList = ignoreMap.get(((Player) player).getUniqueId().toString());
					isIgnored = false;
					for(String s : ignoreList){
						if(s.equals(((Player) msgSender).getUniqueId().toString())){
							isIgnored = true;
						}
					}
				}
				if(senderCurrentChannel.equalsIgnoreCase("noChannel")){
					msgSender.sendMessage(error + "You aren't in a channel! Use /cg for global chat or choose a channel with /channel");
				} else {
					
					if((playerCurrentChannel.equalsIgnoreCase(senderCurrentChannel) || getCurrentChannel().getStringList("ctuned." + senderCurrentChannel).contains(player.getUniqueId().toString())) && !isIgnored){
						String channelColor = "YELLOW";
						if(getConfig().getString("channelcolor." + senderCurrentChannel) != null){
							channelColor = getConfig().getString("channelcolor." + senderCurrentChannel);
						}
						if(!isIgnored) {
							if(msgSender.hasPermission("chatchannels.message.color")) {
								player.sendMessage(ChatColor.valueOf(channelColor) + "[" + senderCurrentChannel + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
							} else {
								player.sendMessage(ChatColor.valueOf(channelColor) + "[" + senderCurrentChannel + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix)  + ChatColor.translateAlternateColorCodes('&', senderName) + suffix + ChatColor.WHITE + ": " + message);
							}		
						}
					} else if((spyMap.get(getUUIDByName(player.getName())) == true)){
						String channelColor = "YELLOW";
						if(getConfig().getString("channelcolor." + senderCurrentChannel) != null){
							channelColor = getConfig().getString("channelcolor." + senderCurrentChannel);
						}
						player.sendMessage(ChatColor.YELLOW + "[spy] "+ ChatColor.valueOf(channelColor) + "[" + senderCurrentChannel + "] " + ChatColor.translateAlternateColorCodes('&', senderName) + ": " + message);
					}
				}
			}
			System.out.println(ChatColor.YELLOW + "[" + senderCurrentChannel + "] " + ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', prefix) + " " + ChatColor.translateAlternateColorCodes('&', senderName) + " " + suffix + " " + ChatColor.WHITE + ": " + ChatColor.translateAlternateColorCodes('&', message));
		}
		event.setCancelled(true);
	}
	
	@EventHandler
	public void onLogin(PlayerLoginEvent event){
		String loginplayer = event.getPlayer().getUniqueId().toString();
		if(getCurrentChannel().getString(loginplayer) == null){
			getCurrentChannel().set(loginplayer, "red");
			saveCurrentChannel();
		}
		if(!spyMap.containsKey(loginplayer)){
			spyMap.put(loginplayer, false);
		}
		if(!muteMap.containsKey(loginplayer)){
			muteMap.put(loginplayer, false);
		}
	}
	//TODO: Implement command replacement
	/*
	@EventHandler
	public void onAlternateCommandUsed(PlayerCommandPreprocessEvent e){
		String[] cmdArray = e.getMessage().split(" ");
		String baseCmd = cmdArray[0];
		StringBuilder sb = new StringBuilder();
		Player p = e.getPlayer();
		if(baseCmd.equalsIgnoreCase("mute")){
			sb.append("/cmute");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		} else if(baseCmd.equalsIgnoreCase("nick")){
			sb.append("/cnick");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		} else if(baseCmd.equalsIgnoreCase("realname")){
			sb.append("/crealname");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		} else if(baseCmd.equalsIgnoreCase("ignore")){
			sb.append("/cignore");
			for(i = 1; i < cmdArray.length; i++) sb.append(" " + cmdArray[i]);
			p.chat(sb.toString());
		}
	}
	*/
	
	@Override
	public void onDisable(){
		plugin = null;
	}

}
