package com.bukkit.HomerBond005.MultiCommand;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;

public class CommandPre extends PlayerListener{
	MultiCommand plugin;
	public CommandPre(MultiCommand got){
		plugin = got;
	}
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if(event.isCancelled()){
			return;
		}
		String[] splitted = event.getMessage().substring(1).split(" ");
		String command = splitted[0];
		String args = " ";
		for(int i = 1; i < splitted.length; i++){
			args += splitted[i] + " ";
		}
		if(args.length() != 0){
			args = args.substring(0, args.length()-1);
		}else{
			args = args.substring(0, args.length());
		}
		if(plugin.getShortcuts().contains(command)){
			String executating = plugin.getShortcutExe(command);
			if(executating.equalsIgnoreCase(command)){
				event.getPlayer().sendMessage(ChatColor.RED + "This would cause a loop! Please edit the config.yml.");
				event.setCancelled(true);
				return;
			}
			event.setCancelled(true);
			event.getPlayer().chat("/" + executating + args);
			return;
		}else{
			return;
		}
	}
}
