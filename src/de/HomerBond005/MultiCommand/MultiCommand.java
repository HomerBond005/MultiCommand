/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.MultiCommand;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiCommand extends JavaPlugin{
	private boolean verbooseMode = false;
	private PluginManager pm;
	private CommandPre playerlistener = new CommandPre(this);
	private PermissionsChecker pc;
	private Metrics metrics;
	private Logger log;
	private Updater updater;
	Map<String, String> shortcuts;
	
	@Override
	public void onEnable(){
		log = getLogger();
		pm = getServer().getPluginManager();
		if(!new File(getDataFolder()+File.separator+"config.yml").exists()){
			getConfig().set("Shortcuts.TheCommandYouExecute", "TheCommandThatShouldBeExecuted");
			getConfig().set("Commands.testList", new ArrayList<String>());
			saveConfig();
			log.log(Level.INFO, "config.yml created.");
		}
		getConfig().addDefault("Shortcuts", new HashMap<String, Object>());
		getConfig().addDefault("Commands", new HashMap<String, Object>());
		getConfig().addDefault("Permissions", true);
		getConfig().addDefault("verbooseMode", false);
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		pm.registerEvents(playerlistener, this);
		pc = new PermissionsChecker(this, getConfig().getBoolean("Permissions", false));
		if(!getConfig().contains("verbooseMode")){
			getConfig().set("verbooseMode", false);
			saveConfig();
			log.log(Level.INFO, "Created verbooseMode in config.yml.");
		}
		verbooseMode = getConfig().getBoolean("verbooseMode", false);
		if(new File("plugins/MultiCommand/Commands").exists()){
			upgrade();
			log.log(Level.INFO, "Filesystem upgraded!");
		}
		loadShortcuts();
		try {
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			log.log(Level.WARNING, "Error while enabling Metrics.");
		}
		updater = new Updater(this);
		getServer().getPluginManager().registerEvents(updater, this);
		log.log(Level.INFO, "is enabled!");
	}
	
	@Override
	public void onDisable(){
		log.log(Level.INFO, "is disabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		if(command.getName().equalsIgnoreCase("muco")){
			Player player;
			if(sender instanceof Player){
				player = (Player) sender;
			}else{
				return handleConsole(args);
			}
			try{
				if(args[0].equalsIgnoreCase("help")){
					throw new ArrayIndexOutOfBoundsException();
				}
			}catch(ArrayIndexOutOfBoundsException e){
				help(player);
				return true;
			}
			if(args[0].equalsIgnoreCase("list")){
				if(!pc.has(player, "MultiCommand.list")&&!pc.has(player, "MultiCommand.all")){
					pc.sendNoPermMsg(player);
					return true;
				}
				player.sendMessage(ChatColor.GREEN + "Following command lists are set:");
				String returned = "";
				Set<String> commands = commands();
				if(commands.size() == 0){
					player.sendMessage(ChatColor.GRAY + "No lists are set.");
				}else{
					for(String actCommand : commands){
						returned += actCommand + ", ";
					}
					returned = returned.substring(0, returned.length()-2);
					player.sendMessage(returned);
				}
				return true;
			}else if(args[0].equalsIgnoreCase("delete")){
				if(args.length == 1){
					player.sendMessage(ChatColor.RED + "Usage: /muco delete <name>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.delete." + args[1])&&!pc.has(player, "MultiCommand.delete.all")&&!pc.has(player, "MultiCommand.all")){
					pc.sendNoPermMsg(player);
					return true;
				}
				if(getConfig().isSet("Commands." +  args[1])){
					getConfig().set("Commands." + args[1], null);
					saveConfig();
					player.sendMessage(ChatColor.GREEN + "Successfully deleted " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exists.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("create")){
				if(!pc.has(player, "MultiCommand.create." + args[1])&&!pc.has(player, "MultiCommand.create.all")&&!pc.has(player, "MultiCommand.all")){
					pc.sendNoPermMsg(player);
					return true;
				}
				if(args.length == 1){
					player.sendMessage(ChatColor.RED + "Usage: /muco create <name>");
					return true;
				}
				if(getConfig().isSet("Commands." + args[1])){
					player.sendMessage(ChatColor.RED + args[1] + " already exists.");
					return true;
				}else{
					getConfig().set("Commands." + args[1], new ArrayList<String>());
					saveConfig();
					player.sendMessage(ChatColor.GREEN + "List " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " successfully created.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("add")){
				if(args.length < 3){
					player.sendMessage(ChatColor.RED + "Usage: /muco add <name> <command>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.add." + args[1])&&!pc.has(player, "MultiCommand.add.all")&&!pc.has(player, "MultiCommand.all")){
					pc.sendNoPermMsg(player);
					return true;
				}
				if(getConfig().isSet("Commands." + args[1])){
					String adding = "";
					for(int i = 2; i < args.length; i++){
						adding += args[i] + " ";
					}
					adding = adding.trim();
					List<String> newList = getConfig().getStringList("Commands." + args[1]);
					newList.add(adding);
					getConfig().set("Commands." + args[1], newList);
					saveConfig();
					player.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.GOLD + adding + ChatColor.GREEN + " to " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exists.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("remove")){
				if(args.length < 3){
					player.sendMessage(ChatColor.RED + "Usage: /muco remove <name> <command>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.remove." + args[1])&&!pc.has(player, "MultiCommand.remove.all")&&!pc.has(player, "MultiCommand.all")){
					pc.sendNoPermMsg(player);
					return true;
				}
				if(getConfig().isSet("Commands." + args[1])){
					String removing = "";
					for(int i = 2; i < args.length; i++){
						removing += args[i] + " ";
					}
					removing = removing.trim();
					List<String> newList = getConfig().getStringList("Commands." + args[1]);
					if(newList.remove(removing)){
						player.sendMessage(ChatColor.GREEN + "Successfully removed '" + removing + "' from " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					}else{
						player.sendMessage(ChatColor.RED + "Error while removing '" + removing + "' from " + ChatColor.GOLD + args[1] + ChatColor.RED + ".");
					}
					getConfig().set("Commands." + args[1], newList);
					saveConfig();
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + args[1] + " doesn't exists.");
					return true;
				}
			}else{
				Set<String> commands = commands();
				for(String actCommand : commands){
					if(args[0].equalsIgnoreCase(actCommand)){
						if(!pc.has(player, "MultiCommand.use." + args[0])&&!pc.has(player, "MultiCommand.use.all")&&!pc.has(player, "MultiCommand.all")){
							pc.sendNoPermMsg(player);
							return true;
						}
						
						List<String> executations = getConfig().getStringList("Commands." + args[0]);
						for(int w = 0; w < executations.size(); w++){
							if(verbooseMode){
								player.sendMessage(ChatColor.DARK_RED + executations.toArray()[w].toString());
							}
							String newchatmsg = executations.toArray()[w].toString();
							Boolean jump = false;
							for(int t = 1; t < 6; t++){
								if((Pattern.compile("\\[\\$" + t + "\\]")).matcher(newchatmsg).find()){
									try{
										newchatmsg = newchatmsg.replaceAll("\\[\\$" + t + "\\]", args[t]);
									}catch(ArrayIndexOutOfBoundsException e){
										newchatmsg = newchatmsg.replaceAll("\\[\\$" + t + "\\]", "");
									}
								}
							}
							newchatmsg = newchatmsg.replaceAll("\\$playername", player.getDisplayName());
							newchatmsg = newchatmsg.replaceAll("\\$playerworld", player.getWorld().getName());
							newchatmsg = newchatmsg.replaceAll("\\$servermaxplayers", "" + getServer().getMaxPlayers());
							newchatmsg = newchatmsg.replaceAll("\\$serveronlineplayers", "" + getServer().getOnlinePlayers().length);
							if((Pattern.compile("\\$1")).matcher(newchatmsg).find()){
								try{
									newchatmsg = newchatmsg.replaceAll("\\$1", args[1]);
								}catch(ArrayIndexOutOfBoundsException e){
									player.sendMessage(ChatColor.RED + "The command above requires at least one argument!");
									player.sendMessage(ChatColor.RED + "Usage: /muco " + args[0] + " <arg1>");
									jump = true;
								}
							}
							if((Pattern.compile("\\$2")).matcher(newchatmsg).find()&&!jump){
								try{
									newchatmsg = newchatmsg.replaceAll("\\$2", args[2]);
								}catch(ArrayIndexOutOfBoundsException e){
									player.sendMessage(ChatColor.RED + "The command above requires at least two arguments!");
									player.sendMessage(ChatColor.RED + "Usage: /muco " + args[0] + " <arg1> <arg2>");
									jump = true;
								}
							}
							if((Pattern.compile("\\$3")).matcher(newchatmsg).find()&&!jump){
								try{
									newchatmsg = newchatmsg.replaceAll("\\$3", args[3]);
								}catch(ArrayIndexOutOfBoundsException e){
									player.sendMessage(ChatColor.RED + "The command above requires at least three arguments!");
									player.sendMessage(ChatColor.RED + "Usage: /muco " + args[0] + " <arg1> <arg2> <arg3>");
									jump = true;
								}
							}
							if((Pattern.compile("\\$4")).matcher(newchatmsg).find()&&!jump){
								try{
									newchatmsg = newchatmsg.replaceAll("\\$4", args[4]);
								}catch(ArrayIndexOutOfBoundsException e){
									player.sendMessage(ChatColor.RED + "The command above requires at least four arguments!");
									player.sendMessage(ChatColor.RED + "Usage: /muco " + args[0] + " <arg1> <arg2> <arg3> <arg4>");
									jump = true;
								}
							}
							if((Pattern.compile("\\$5")).matcher(newchatmsg).find()&&!jump){
								try{
									newchatmsg = newchatmsg.replaceAll("\\$5", args[5]);
								}catch(ArrayIndexOutOfBoundsException e){
									player.sendMessage(ChatColor.RED + "The command above requires at least three arguments!");
									player.sendMessage(ChatColor.RED + "Usage: /muco " + args[0] + " <arg1> <arg2> <arg3> <arg4> <arg5>");
									jump = true;
								}
							}
							if(jump == false){
								player.chat(newchatmsg);
							}
						}
						return true;
					}
				}
				player.sendMessage(ChatColor.RED + "List " + ChatColor.GOLD + args[0] + ChatColor.RED + " doesn't exists.");
			}
		}
		return true;
	}
	
	private Set<String> commands(){
		return getConfig().getConfigurationSection("Commands").getKeys(false);
	}
	
	private void help(Player player){
		if(!pc.has(player, "MultiCommand.help")){
			pc.sendNoPermMsg(player);
			return;
		}
		player.sendMessage(ChatColor.GRAY + "MultiCommand Help");
		player.sendMessage(ChatColor.RED + "/muco help    " + ChatColor.YELLOW + "Shows this page.");
		player.sendMessage(ChatColor.RED + "/muco list    " + ChatColor.YELLOW + "Listes all lists of commands.");
		player.sendMessage(ChatColor.RED + "/muco <name>    " + ChatColor.YELLOW + "Executes a list of commands.");
		player.sendMessage(ChatColor.RED + "/muco create <name>    " + ChatColor.YELLOW + "Adds a list of commands.");
		player.sendMessage(ChatColor.RED + "/muco add <name> <command>    " + ChatColor.YELLOW + "Adds a command to a list.");
		player.sendMessage(ChatColor.RED + "/muco remove <name> <command>    " + ChatColor.YELLOW + "Removes a command from a list.");
		player.sendMessage(ChatColor.RED + "/muco delete <name>    " + ChatColor.YELLOW + "Deletes a list of commands.");
	}
	
	private void loadShortcuts(){
		reloadConfig();
		if(!getConfig().isSet("Shortcuts")){
			getConfig().set("Shortcuts", new HashMap<String, Object>());
			saveConfig();
		}
		Set<String> section = getConfig().getConfigurationSection("Shortcuts").getKeys(false);
		Map<String, String> shortcutsTemp = new HashMap<String, String>();
		for(String shortcut : section){
			shortcutsTemp.put(shortcut.toLowerCase(), getConfig().getString("Shortcuts." + shortcut));
		}
		shortcuts = shortcutsTemp;
	}
	
	private boolean handleConsole(String[] args){
		try{
			if(args[0].equalsIgnoreCase("help")){
				throw new ArrayIndexOutOfBoundsException();
			}
		}catch(ArrayIndexOutOfBoundsException e){
			log.log(Level.INFO, "Help");
			log.log(Level.INFO, "muco help    " + "Shows this page.");
			log.log(Level.INFO, "muco list    " + "Listes all lists of commands.");
			log.log(Level.INFO, "muco <name>    " + "Executes a list of commands.");
			log.log(Level.INFO, "muco create <name>    " + "Adds a list of commands.");
			log.log(Level.INFO, "muco add <name> <command>    " + "Adds a command to a list.");
			log.log(Level.INFO, "muco remove <name> <command>    " + "Removes a command from a list.");
			log.log(Level.INFO, "muco delete <name>    " + "Deletes a list of commands.");
			return true;
		}
		if(args[0].equalsIgnoreCase("list")){
			log.log(Level.INFO, "Following command lists are set:");
			String returned = "";
			Set<String> commands = commands();
			if(commands.size() == 0){
				log.log(Level.INFO, "No lists are set.");
			}else{
				for(String actCommand : commands){
					returned += actCommand + ", ";
				}
				returned = returned.substring(0, returned.length()-2);
				log.log(Level.INFO, returned);
			}
			return true;
		}else if(args[0].equalsIgnoreCase("delete")){
			if(args.length == 1){
				log.log(Level.INFO, "Usage: /muco delete <name>");
				return true;
			}
			if(getConfig().isSet("Commands." +  args[1])){
				getConfig().set("Commands." + args[1], null);
				saveConfig();
				log.log(Level.INFO, "Successfully deleted " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
				return true;
			}else{
				log.log(Level.INFO, "List " + args[1] + " doesn't exists.");
				return true;
			}
		}else if(args[0].equalsIgnoreCase("create")){
			if(args.length == 1){
				log.log(Level.INFO, "Usage: /muco create <name>");
				return true;
			}
			if(getConfig().isSet("Commands." + args[1])){
				log.log(Level.INFO, args[1] + " already exists.");
				return true;
			}else{
				getConfig().set("Commands." + args[1], new ArrayList<String>());
				saveConfig();
				log.log(Level.INFO, "List " + args[1] + " successfully created.");
				return true;
			}
		}else if(args[0].equalsIgnoreCase("add")){
			if(args.length < 3){
				log.log(Level.INFO, "Usage: /muco add <name> <command>");
				return true;
			}
			if(getConfig().isSet("Commands." + args[1])){
				String adding = "";
				for(int i = 2; i < args.length; i++){
					adding += args[i] + " ";
				}
				adding = adding.trim();
				List<String> newList = getConfig().getStringList("Commands." + args[1]);
				newList.add(adding);
				getConfig().set("Commands." + args[1], newList);
				saveConfig();
				log.log(Level.INFO, "Successfully added " + adding + " to " + args[1] + ".");
				return true;
			}else{
				log.log(Level.INFO, "List " + args[1] + " doesn't exists.");
				return true;
			}
		}else if(args[0].equalsIgnoreCase("remove")){
			if(args.length < 3){
				log.log(Level.INFO, "Usage: /muco remove <name> <command>");
				return true;
			}
			if(getConfig().isSet("Commands." + args[1])){
				String removing = "";
				for(int i = 2; i < args.length; i++){
					removing += args[i] + " ";
				}
				removing = removing.trim();
				List<String> newList = getConfig().getStringList("Commands." + args[1]);
				if(newList.remove(removing)){
					log.log(Level.INFO, "Successfully removed '" + removing + "' from " + args[1] + ".");
				}else{
					log.log(Level.INFO, "Error while removing '" + removing + "' from " + args[1] + ".");
				}
				getConfig().set("Commands." + args[1], newList);
				saveConfig();
				return true;
			}else{
				log.log(Level.INFO, "List " + args[1] + " doesn't exists.");
				return true;
			}
		}else{
			Set<String> commands = commands();
			for(String actCommand : commands){
				if(args[0].equalsIgnoreCase(actCommand)){
					List<String> executations = getConfig().getStringList("Commands." + args[0]);
					for(int w = 0; w < executations.size(); w++){
						String newchatmsg = executations.toArray()[w].toString().substring(1);
						if(verbooseMode){
							log.log(Level.INFO, newchatmsg);
						}
						Boolean jump = false;
						for(int t = 1; t < 6; t++){
							if((Pattern.compile("\\[\\$" + t + "\\]")).matcher(newchatmsg).find()){
								try{
									newchatmsg = newchatmsg.replaceAll("\\[\\$" + t + "\\]", args[t]);
								}catch(ArrayIndexOutOfBoundsException e){
									newchatmsg = newchatmsg.replaceAll("\\[\\$" + t + "\\]", "");
								}
							}
						}
						newchatmsg = newchatmsg.replaceAll("\\$playername", "Console");
						newchatmsg = newchatmsg.replaceAll("\\$playerworld", "Console");
						newchatmsg = newchatmsg.replaceAll("\\$servermaxplayers", "" + getServer().getMaxPlayers());
						newchatmsg = newchatmsg.replaceAll("\\$serveronlineplayers", "" + getServer().getOnlinePlayers().length);
						if((Pattern.compile("\\$1")).matcher(newchatmsg).find()){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$1", args[1]);
							}catch(ArrayIndexOutOfBoundsException e){
								log.log(Level.INFO, "The command above requires at least one argument!");
								log.log(Level.INFO, "Usage: /muco " + args[0] + " <arg1>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$2")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$2", args[2]);
							}catch(ArrayIndexOutOfBoundsException e){
								log.log(Level.INFO, "The command above requires at least two arguments!");
								log.log(Level.INFO, "Usage: /muco " + args[0] + " <arg1> <arg2>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$3")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$3", args[3]);
							}catch(ArrayIndexOutOfBoundsException e){
								log.log(Level.INFO, "The command above requires at least three arguments!");
								log.log(Level.INFO, "Usage: /muco " + args[0] + " <arg1> <arg2> <arg3>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$4")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$4", args[4]);
							}catch(ArrayIndexOutOfBoundsException e){
								log.log(Level.INFO, "The command above requires at least four arguments!");
								log.log(Level.INFO, "Usage: /muco " + args[0] + " <arg1> <arg2> <arg3> <arg4>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$5")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$5", args[5]);
							}catch(ArrayIndexOutOfBoundsException e){
								log.log(Level.INFO, "The command above requires at least three arguments!");
								log.log(Level.INFO, "Usage: /muco " + args[0] + " <arg1> <arg2> <arg3> <arg4> <arg5>");
								jump = true;
							}
						}
						if(jump == false){
							getServer().dispatchCommand(getServer().getConsoleSender(), newchatmsg);
						}
					}
					return true;
				}
			}
			log.log(Level.INFO, "List " + args[0] + " doesn't exists.");
			return true;
		}
	}
	
	private void upgrade(){
		File commandsdir = new File(getDataFolder()+File.separator+"Commands");
		File[] files = commandsdir.listFiles();
		for(int i = 0; i < files.length; i++){
			int yml = files[i].getName().length() - 4;
			String name = files[i].getName().substring(0, yml);
			Set<String> commands = YamlConfiguration.loadConfiguration(new File(commandsdir + File.separator + name + ".yml")).getKeys(false);
			getConfig().set("Commands." + name, new ArrayList<String>(commands));
			files[i].delete();
		}
		commandsdir.delete();
		saveConfig();
		reloadConfig();
	}
}
