/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.MultiCommand;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

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
		if(args.length() != 0){
			args = args.substring(0, args.length()-1);
		}else{
			args = args.substring(0, args.length());
		}
		command = command.toLowerCase();
		if(plugin.shortcuts.containsKey(command)){
			String executating = plugin.shortcuts.get(command);
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
