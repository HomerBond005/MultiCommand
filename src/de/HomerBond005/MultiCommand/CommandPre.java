/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.MultiCommand;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandPre implements Listener{
	MultiCommand plugin;
	
	public CommandPre(MultiCommand got){
		plugin = got;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
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
		args = args.substring(0, args.length()-1);
		command = command.toLowerCase();
		if(plugin.getShortcuts().containsKey(command)){
			String executating = plugin.getShortcuts().get(command);
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
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerCommand(ServerCommandEvent event){
		String[] splitted = event.getCommand().split(" ");
		String command = splitted[0];
		String args = " ";
		for(int i = 1; i < splitted.length; i++){
			args += splitted[i] + " ";
		}
		args = args.substring(0, args.length()-1);
		command = command.toLowerCase();
		if(plugin.getShortcuts().containsKey(command)){
			String executating = plugin.getShortcuts().get(command);
			if(executating.equalsIgnoreCase(command)){
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This would cause a loop! Please edit the config.yml.");
				event.setCommand("nothingatall");
				return;
			}
			event.setCommand("nothingatall");
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executating + args);
			return;
		}else{
			return;
		}
	}
}
