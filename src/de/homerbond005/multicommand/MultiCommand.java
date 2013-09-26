/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.homerbond005.multicommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.homerbond005.multicommand.Metrics.Graph;

public class MultiCommand extends JavaPlugin {
	private boolean verbooseMode;
	private boolean playerDisplayName;
	private PluginManager pm;
	private CommandPre playerlistener;
	private Metrics metrics;
	private Logger log;
	private Map<String, String> shortcuts;
	private LinkedList<String> disabledCommands;
	private int maxvariables;
	private String commandDisabledMsg;

	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		log = getLogger();
		pm = getServer().getPluginManager();
		playerlistener = new CommandPre(this);
		pm.registerEvents(playerlistener, this);
		reload();
		try {
			metrics = new Metrics(this);
			String usingVerboose;
			if (verbooseMode) {
				usingVerboose = "Using verboose mode";
			} else {
				usingVerboose = "Not using verboose mode";
			}
			Graph graphverboose = metrics.createGraph("Default");
			graphverboose.addPlotter(new Metrics.Plotter(usingVerboose) {
				@Override
				public int getValue() {
					return 1;
				}
			});
			String playername;
			if (playerDisplayName)
				playername = "Display name";
			else
				playername = "Player name";
			Graph graphplayername = metrics.createGraph("Player name layout");
			graphplayername.addPlotter(new Metrics.Plotter(playername) {
				@Override
				public int getValue() {
					return 1;
				}
			});
			metrics.start();
		} catch (IOException e) {
			log.log(Level.WARNING, "Error while enabling Metrics.");
		}
		log.log(Level.INFO, "is enabled!");
	}

	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		log.log(Level.INFO, "is disabled!");
	}

	/**
	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
	 *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean onCommand(final CommandSender sender, Command command,
			String commandLabel, String[] args) {
		if (command.getName().equalsIgnoreCase("muco")) {
			Player player = null;
			if (sender instanceof Player) {
				player = (Player) sender;
			}
			if (args.length == 0)
				args = new String[] { "help" };
			if (args[0].equalsIgnoreCase("help")) {
				if (!checkPerm(sender, "MultiCommand.help")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
				} else {
					sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "MultiCommand" + ChatColor.GRAY + ChatColor.BOLD + " Help");
					sender.sendMessage(ChatColor.GOLD + "/muco help " + ChatColor.GRAY + "Shows this page.");
					sender.sendMessage(ChatColor.GOLD + "/muco list " + ChatColor.GRAY + "Listes all lists of commands.");
					sender.sendMessage(ChatColor.GOLD + "/muco reload " + ChatColor.GRAY + "Reload the config.");
					sender.sendMessage(ChatColor.GOLD + "/muco <name> " + ChatColor.GRAY + "Executes a list of commands.");
					sender.sendMessage(ChatColor.GOLD + "/muco create <name> " + ChatColor.GRAY + "Adds a list of commands.");
					sender.sendMessage(ChatColor.GOLD + "/muco add <name> <command> " + ChatColor.GRAY + "Adds a command to a list.");
					sender.sendMessage(ChatColor.GOLD + "/muco remove <name> <command> " + ChatColor.GRAY + "Removes a command from a list.");
					sender.sendMessage(ChatColor.GOLD + "/muco delete <name> " + ChatColor.GRAY + "Deletes a list of commands.");
					sender.sendMessage(ChatColor.GOLD + "/muco show <name> " + ChatColor.GRAY + "Shows all commands in a list.");
					sender.sendMessage(ChatColor.GOLD + "/muco disable " + ChatColor.GRAY + "Options for disabling commands.");
				}
				return true;
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!checkPerm(sender, "MultiCommand.reload") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				reload();
				sender.sendMessage(ChatColor.GREEN + "Successfully reloaded MultiCommand!");
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if (!checkPerm(sender, "MultiCommand.list") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				sender.sendMessage(ChatColor.GOLD + "Following command lists are set:");
				String returned = "";
				Set<String> commands = commands();
				if (commands.size() == 0) {
					sender.sendMessage(ChatColor.GRAY + "No lists are set.");
				} else {
					for (String actCommand : commands) {
						returned += actCommand + ", ";
					}
					returned = returned.substring(0, returned.length() - 2);
					sender.sendMessage(returned);
				}
				return true;
			} else if (args[0].equalsIgnoreCase("show")) {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Usage: /muco show <name>");
					return true;
				}
				if (!checkPerm(sender, "MultiCommand.show." + args[0]) && !!checkPerm(sender, "MultiCommand.show.all") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				if (getConfig().isSet("Commands." + args[1])) {
					List<String> executations = getConfig().getStringList("Commands." + args[1]);
					int count = 1;
					if (executations.size() == 0)
						sender.sendMessage(ChatColor.GRAY + "There are no commands in this list.");
					for (String exe : executations) {
						sender.sendMessage(ChatColor.GRAY + "" + count + ". " + ChatColor.GOLD + exe);
						count++;
					}
				} else {
					sender.sendMessage(ChatColor.RED + "The list " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exist.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("delete")) {
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Usage: /muco delete <name>");
					return true;
				}
				if (!checkPerm(sender, "MultiCommand.delete." + args[1]) && !checkPerm(sender, "MultiCommand.delete.all") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				if (getConfig().isSet("Commands." + args[1])) {
					getConfig().set("Commands." + args[1], null);
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully deleted " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "The list " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exist.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("create")) {
				if (!checkPerm(sender, "MultiCommand.create." + args[1]) && !checkPerm(sender, "MultiCommand.create.all") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				if (args.length == 1) {
					sender.sendMessage(ChatColor.RED + "Usage: /muco create <name>");
					return true;
				}
				if (getConfig().isSet("Commands." + args[1])) {
					sender.sendMessage(ChatColor.GOLD + args[1] + ChatColor.RED + " already exists.");
					return true;
				} else {
					getConfig().set("Commands." + args[1], new ArrayList<String>());
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + "The list " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " was successfully created.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("add")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /muco add <name> <command>");
					return true;
				}
				if (!checkPerm(sender, "MultiCommand.add." + args[1]) && !checkPerm(sender, "MultiCommand.add.all") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				if (getConfig().isSet("Commands." + args[1])) {
					String adding = "";
					for (int i = 2; i < args.length; i++) {
						adding += args[i] + " ";
					}
					adding = adding.trim();
					List<String> newList = getConfig().getStringList("Commands." + args[1]);
					newList.add(adding);
					getConfig().set("Commands." + args[1], newList);
					saveConfig();
					sender.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.GOLD + adding + ChatColor.GREEN + " to " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "List " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exists.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("remove")) {
				if (args.length < 3) {
					sender.sendMessage(ChatColor.RED + "Usage: /muco remove <name> <command>");
					return true;
				}
				if (!checkPerm(sender, "MultiCommand.remove." + args[1]) && !checkPerm(sender, "MultiCommand.remove.all") && !checkPerm(sender, "MultiCommand.all")) {
					player.sendMessage(ChatColor.RED + "You do not have the required permission!");
					return true;
				}
				if (getConfig().isSet("Commands." + args[1])) {
					String removing = "";
					for (int i = 2; i < args.length; i++) {
						removing += args[i] + " ";
					}
					removing = removing.trim();
					List<String> newList = getConfig().getStringList("Commands." + args[1]);
					if (newList.remove(removing)) {
						sender.sendMessage(ChatColor.GREEN + "Successfully removed '" + removing + "' from " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					} else {
						sender.sendMessage(ChatColor.RED + "Error while removing '" + removing + "' from " + ChatColor.GOLD + args[1] + ChatColor.RED + ".");
					}
					getConfig().set("Commands." + args[1], newList);
					saveConfig();
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "The list " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exist.");
					return true;
				}
			} else if (args[0].equalsIgnoreCase("disable")) {
				if (args.length == 1 || (args.length == 2 && args[1].equalsIgnoreCase("help"))) {
					sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "MultiCommand" + ChatColor.GRAY + ChatColor.BOLD + " Command Disable Help");
					sender.sendMessage(ChatColor.GOLD + "/muco disable list " + ChatColor.GRAY + "Listes all disabled commands.");
					sender.sendMessage(ChatColor.GOLD + "/muco disable disable <command> " + ChatColor.GRAY + "Disable a command.");
					sender.sendMessage(ChatColor.GOLD + "/muco disable enable <command> " + ChatColor.GRAY + "Enable a disabled command.");
					return true;
				}
				if (args[1].equalsIgnoreCase("list")) {
					if (checkPerm(sender, "MultiCommand.disable.list")) {
						sender.sendMessage(ChatColor.GRAY + "The following commands are disabled:");
						for (String com : disabledCommands) {
							sender.sendMessage(ChatColor.GOLD + com);
						}
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You do not have the required permission!");
						return true;
					}
				}
				if (args[1].equalsIgnoreCase("disable")) {
					if (checkPerm(sender, "MultiCommand.disable.disable")) {
						if (args.length >= 3) {
							String com = "";
							for (int i = 2; i < args.length; i++) {
								com += args[i] + " ";
							}
							com = com.trim();
							if (!disabledCommands.contains(com)) {
								disabledCommands.add(com);
								reloadConfig();
								getConfig().set("DisabledCommands", disabledCommands);
								saveConfig();
								sender.sendMessage(ChatColor.GREEN + "Successfully disabled the command " + ChatColor.GOLD + com + ChatColor.GREEN + ".");
							} else
								sender.sendMessage(ChatColor.RED + "The command " + ChatColor.GOLD + com + ChatColor.RED + " is already disabled!");
						} else
							sender.sendMessage(ChatColor.RED + "Usage: /muco disable disable <command>");
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You do not have the required permission!");
						return true;
					}
				}
				if (args[1].equalsIgnoreCase("enable")) {
					if (checkPerm(sender, "MultiCommand.disable.enable")) {
						if (args.length >= 3) {
							String com = "";
							for (int i = 2; i < args.length; i++) {
								com += args[i] + " ";
							}
							com = com.trim();
							if (disabledCommands.contains(com)) {
								disabledCommands.remove(com);
								reloadConfig();
								getConfig().set("DisabledCommands", disabledCommands);
								saveConfig();
								sender.sendMessage(ChatColor.GREEN + "Successfully enabled the command " + ChatColor.GOLD + com + ChatColor.GREEN + ".");
							} else
								sender.sendMessage(ChatColor.RED + "The command " + ChatColor.GOLD + com + ChatColor.RED + " is not disabled!");
						} else
							sender.sendMessage(ChatColor.RED + "Usage: /muco disable enable <command>");
						return true;
					} else {
						player.sendMessage(ChatColor.RED + "You do not have the required permission!");
						return true;
					}
				}
			} else {
				Set<String> commands = commands();
				for (String actCommand : commands) {
					if (args[0].equalsIgnoreCase(actCommand)) {
						if (!checkPerm(sender, "MultiCommand.use." + args[0]) && !checkPerm(sender, "MultiCommand.use.all") && !checkPerm(sender, "MultiCommand.all")) {
							player.sendMessage(ChatColor.RED + "You do not have the required permission!");
							return true;
						}
						List<String> executations = getConfig().getStringList("Commands." + args[0]);
						long totaldelay = 0L;
						for (int w = 0; w < executations.size(); w++) {
							String chatmsg = executations.get(w);
							for (int t = 1; t < maxvariables + 1; t++) {
								if ((Pattern.compile("\\[\\$" + t + "\\]")).matcher(chatmsg).find()) {
									try {
										chatmsg = chatmsg.replaceAll("\\[\\$" + t + "\\]", args[t]);
									} catch (ArrayIndexOutOfBoundsException e) {
										chatmsg = chatmsg.replaceAll("\\[\\$" + t + "\\]", "");
									}
								}
							}
							Matcher consolematcher = Pattern.compile("^\\[\\$c\\]").matcher(chatmsg);
							boolean console = false;
							if (consolematcher.find()) {
								console = true;
								chatmsg = consolematcher.replaceFirst("");
							}
							Matcher delaymatcher = Pattern.compile("^\\[\\d+\\]").matcher(chatmsg);
							if (delaymatcher.find()) {
								totaldelay += 20 * Integer.parseInt(delaymatcher.group(0).replaceAll("\\[", "").replaceAll("\\]", ""));
								chatmsg = delaymatcher.replaceFirst("");
							}
							if (player == null || console) {
								Matcher slashmatcher = Pattern.compile("^\\/").matcher(chatmsg);
								if (slashmatcher.find()) {
									chatmsg = slashmatcher.replaceFirst("");
								}
							}
							String removedoptionalargs = chatmsg;
							if (player == null) {
								chatmsg = chatmsg.replaceAll("\\$playername", "Console");
								chatmsg = chatmsg.replaceAll("\\$playerworld", "Console");
							} else {
								if (playerDisplayName)
									chatmsg = chatmsg.replaceAll("\\$playername", player.getDisplayName());
								else
									chatmsg = chatmsg.replaceAll("\\$playername", player.getName());
								chatmsg = chatmsg.replaceAll("\\$playerworld", player.getWorld().getName());
							}
							chatmsg = chatmsg.replaceAll("\\$servermaxplayers", "" + getServer().getMaxPlayers());
							chatmsg = chatmsg.replaceAll("\\$serveronlineplayers", "" + getServer().getOnlinePlayers().length);
							boolean error = false;
							for (int t = 1; t < maxvariables + 1; t++) {
								if ((Pattern.compile("\\$" + t)).matcher(chatmsg).find()) {
									try {
										chatmsg = chatmsg.replaceAll("\\$" + t, args[t]);
									} catch (ArrayIndexOutOfBoundsException e) {
										error = true;
										break;
									}
								}
							}
							if (error) {
								sender.sendMessage(ChatColor.RED + "The command '" + ChatColor.GRAY + executations.get(w) + ChatColor.RED + "' requires more arguments!");
								String msg = ChatColor.RED + "Usage: /muco " + args[0];
								for (int t = 1; t < maxvariables + 1; t++) {
									if ((Pattern.compile("\\$" + t)).matcher(removedoptionalargs).find()) {
										msg += " <arg" + t + ">";
									}
								}
								sender.sendMessage(msg);
							} else {
								final String finalchatmsg = chatmsg;
								final long finaldelay = totaldelay;
								if (player == null || console) {
									getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										@Override
										public void run() {
											if (verbooseMode)
												sender.sendMessage(ChatColor.RED + "Executing " + ChatColor.DARK_RED + finalchatmsg + ChatColor.RED + " after " + ChatColor.DARK_RED + (finaldelay / 20) + ChatColor.RED + " seconds after the command executation.");
											getServer().dispatchCommand(getServer().getConsoleSender(), finalchatmsg);
										}
									}, totaldelay);
								} else {
									final Player finalplayer = player;
									getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
										@Override
										public void run() {
											if (verbooseMode)
												finalplayer.sendMessage(ChatColor.RED + "Executing " + ChatColor.DARK_RED + finalchatmsg + ChatColor.RED + " after " + ChatColor.DARK_RED + (finaldelay / 20) + ChatColor.RED + " seconds after the command executation.");
											finalplayer.chat(finalchatmsg);
										}
									}, totaldelay);
								}
							}
						}
						return true;
					}
				}
				sender.sendMessage(ChatColor.RED + "The list " + ChatColor.GOLD + args[0] + ChatColor.RED + " doesn't exist.");
			}
		}
		return true;
	}

	/**
	 * Reload the attributes from MultiCommand
	 */
	private void reload() {
		if (!new File(getDataFolder() + File.separator + "config.yml").exists()) {
			getConfig().set("Shortcuts.TheCommandYouExecute", "TheCommandThatShouldBeExecuted");
			getConfig().set("Commands.testList", new ArrayList<String>());
			saveConfig();
			log.log(Level.INFO, "config.yml created.");
		}
		reloadConfig();
		getConfig().addDefault("Shortcuts", new HashMap<String, Object>());
		getConfig().addDefault("Commands", new HashMap<String, Object>());
		getConfig().addDefault("DisabledCommands", new LinkedList<String>());
		getConfig().addDefault("commandDisabledMsg", "&cThis command is disabled!");
		getConfig().addDefault("Permissions", true);
		getConfig().addDefault("verbooseMode", false);
		getConfig().addDefault("playerDisplayName", true);
		getConfig().addDefault("maxvariables", 5);
		getConfig().addDefault("updateReminderEnabled", true);
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		verbooseMode = getConfig().getBoolean("verbooseMode");
		playerDisplayName = getConfig().getBoolean("playerDisplayName");
		maxvariables = getConfig().getInt("maxvariables");
		commandDisabledMsg = getConfig().getString("commandDisabledMsg");
		loadCommands();
	}

	/**
	 * Fetch the commands from the config
	 * 
	 * @return
	 */
	private Set<String> commands() {
		return getConfig().getConfigurationSection("Commands").getKeys(false);
	}

	/**
	 * Get all disabled commands
	 * 
	 * @return All disabled commands in a String list with all arguments that
	 *         are given
	 */
	public LinkedList<String> getDisabledCommands() {
		return disabledCommands;
	}

	public String commandDisabledMsg() {
		return commandDisabledMsg;
	}

	/**
	 * Load the shortcuts from the config
	 */
	private void loadCommands() {
		reloadConfig();
		if (!getConfig().isSet("Shortcuts")) {
			getConfig().set("Shortcuts", new HashMap<String, Object>());
			saveConfig();
		}
		Set<String> section = getConfig().getConfigurationSection("Shortcuts").getKeys(false);
		shortcuts = new HashMap<String, String>();
		for (String shortcut : section) {
			shortcuts.put(shortcut.toLowerCase(), getConfig().getString("Shortcuts." + shortcut));
		}

		disabledCommands = new LinkedList<String>();
		for (String cmd : getConfig().getStringList("DisabledCommands")) {
			disabledCommands.add(cmd.trim().toLowerCase());
		}
	}

	/**
	 * Check if a CommandSender has a permission
	 * 
	 * @param sender The CommandSender object
	 * @param perm The permission that should be checked
	 * @return Does the CommandSender has the permission?
	 */
	private boolean checkPerm(CommandSender sender, String perm) {
		if (sender instanceof Player) {
			return ((Player) sender).hasPermission(perm);
		} else
			return true;
	}

	/**
	 * Get the shortcuts
	 * 
	 * @return The shortcuts
	 */
	public Map<String, String> getShortcuts() {
		return shortcuts;
	}
}
