/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.homerbond005.multicommand;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class CommandPre implements Listener {
	MultiCommand plugin;

	public CommandPre(MultiCommand got) {
		plugin = got;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		String prepared = event.getMessage().trim().substring(1);
		LinkedList<String> discom = plugin.getDisabledCommands();
		boolean isDisabled = false;
		for (String com : discom) {
			if (("/" + prepared).startsWith(com)) {
				isDisabled = true;
				break;
			}
		}
		String[] splitted = prepared.split(" ");
		String command = splitted[0];
		if (isDisabled)
			if (!player.hasPermission("MultiCommand.ignoreDisabledCommands") && !player.hasPermission("MultiCommand.ignoreDisabledCommand." + command.toLowerCase())) {
				if (plugin.commandDisabledMsg().trim().length() != 0)
					player.sendMessage(plugin.commandDisabledMsg().replaceAll("(&([a-f0-9]))", "\u00A7$2"));
				event.setCancelled(true);
				return;
			}
		String args = " ";
		for (int i = 1; i < splitted.length; i++) {
			args += splitted[i] + " ";
		}
		args = args.substring(0, args.length() - 1);
		command = command.toLowerCase();
		if (plugin.getShortcuts().containsKey(command)) {
			String executating = plugin.getShortcuts().get(command);
			if (executating.equalsIgnoreCase(command)) {
				player.sendMessage(ChatColor.RED + "This would cause a loop! Please edit the config.yml.");
				event.setCancelled(true);
			} else {
				event.setCancelled(true);
				player.chat("/" + executating + args);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerCommand(ServerCommandEvent event) {
		String[] splitted = event.getCommand().split(" ");
		String command = splitted[0];
		String args = " ";
		for (int i = 1; i < splitted.length; i++) {
			args += splitted[i] + " ";
		}
		args = args.substring(0, args.length() - 1);
		command = command.toLowerCase();
		if (plugin.getShortcuts().containsKey(command)) {
			String executating = plugin.getShortcuts().get(command);
			event.setCommand("nothingatall");
			if (executating.equalsIgnoreCase(command)) {
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This would cause a loop! Please edit the config.yml.");
				return;
			}
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executating + args);
			return;
		} else {
			return;
		}
	}
}
