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
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics.Metrics;
import de.HomerBond005.Permissions.PermissionsChecker;

public class MultiCommand extends JavaPlugin{
	private String mainDir = "plugins/MultiCommand";
	private File configfile = new File (mainDir + File.separator + "config.yml");
	private FileConfiguration bukkitconfig = YamlConfiguration.loadConfiguration(configfile);
	private boolean verbooseMode = false;
	private PluginManager pm;
	private CommandPre playerlistener = new CommandPre(this);
	private PermissionsChecker pc;
	private Metrics metrics;
	Map<String, String> shortcuts;
	public void onEnable(){
		pm = getServer().getPluginManager();
		if(!new File(mainDir).exists()){
			new File(mainDir).mkdir();
			System.out.println("[MultiCommand]: /plugins/MultiCommand created.");
		}
		if(!configfile.exists()){
			try{
				configfile.createNewFile();
				bukkitconfig.set("Permissions", true);
				bukkitconfig.set("Shortcuts.TheCommandYouExecute", "TheCommandThatShouldBeExecuted");
				bukkitconfig.set("verbooseMode", false);
				bukkitconfig.set("Commands.testList", new ArrayList<String>());
				bukkitconfig.save(configfile);
				System.out.println("[MultiCommand]: config.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		pm.registerEvents(playerlistener, this);
		pc = new PermissionsChecker(this, bukkitconfig.getBoolean("Permissions", false));
		if(!bukkitconfig.contains("verbooseMode")){
			bukkitconfig.set("verbooseMode", false);
			try{
				bukkitconfig.save(configfile);
			}catch(IOException e){}
			System.out.println("[MultiCommand]: Created verbooseMode in config.yml.");
		}
		verbooseMode = bukkitconfig.getBoolean("verbooseMode", false);
		if(new File("plugins/MultiCommand/Commands").exists()){
			upgrade();
			System.out.println("[MultiCommand]: Filesystem upgraded!");
		}
		loadShortcuts();
		try {
			metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			System.err.println("[MultiCommand]: Error while enabling Metrics.");
		}
		System.out.println("[MultiCommand] is enabled!");
	}
	public void onDisable(){
		System.out.println("[MultiCommand] is disabled!");
	}
	private Set<String> commands(){
		return bukkitconfig.getConfigurationSection("Commands").getKeys(false);
	}
	private void help(Player player){
		if(!pc.has(player, "MultiCommand.help")){
			sendNoPermMsg(player);
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
		try{
			bukkitconfig.load(configfile);
		}catch(Exception e){
		}
		Set<String> section = bukkitconfig.getConfigurationSection("Shortcuts").getKeys(false);
		Map<String, String> shortcutsTemp = new HashMap<String, String>();
		for(String shortcut : section){
			shortcutsTemp.put(shortcut.toLowerCase(), bukkitconfig.getString("Shortcuts." + shortcut));
		}
		shortcuts = shortcutsTemp;
	}
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
					sendNoPermMsg(player);
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
					sendNoPermMsg(player);
					return true;
				}
				if(bukkitconfig.isSet("Commands." +  args[1])){
					bukkitconfig.set("Commands." + args[1], null);
					try{
						bukkitconfig.save(configfile);
					}catch(IOException e){
						player.sendMessage(ChatColor.RED + "Error while deleting " + ChatColor.GOLD + args[1] + ChatColor.RED + "!");
					}
					player.sendMessage(ChatColor.GREEN + "Successfully deleted " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + ChatColor.GOLD + args[1] + ChatColor.RED + " doesn't exists.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("create")){
				if(!pc.has(player, "MultiCommand.create." + args[1])&&!pc.has(player, "MultiCommand.create.all")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				if(args.length == 1){
					player.sendMessage(ChatColor.RED + "Usage: /muco create <name>");
					return true;
				}
				if(bukkitconfig.isSet("Commands." + args[1])){
					player.sendMessage(ChatColor.RED + args[1] + " already exists.");
					return true;
				}else{
					bukkitconfig.set("Commands." + args[1], new ArrayList<String>());
					try {
						bukkitconfig.save(configfile);
					} catch (IOException e) {
						player.sendMessage(ChatColor.RED + "Failed on creating a new list!");
						e.printStackTrace();
					}
					player.sendMessage(ChatColor.GREEN + "List " + ChatColor.GOLD + args[1] + ChatColor.GREEN + " successfully created.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("add")){
				if(args.length < 3){
					player.sendMessage(ChatColor.RED + "Usage: /muco add <name> <command>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.add." + args[1])&&!pc.has(player, "MultiCommand.add.all")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				if(bukkitconfig.isSet("Commands." + args[1])){
					String adding = "";
					for(int i = 2; i < args.length; i++){
						adding += args[i] + " ";
					}
					adding = adding.trim();
					try {
						List<String> newList = bukkitconfig.getStringList("Commands." + args[1]);
						newList.add(adding);
						bukkitconfig.set("Commands." + args[1], newList);
						bukkitconfig.save(configfile);
					} catch (Exception e){
					}
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
					sendNoPermMsg(player);
					return true;
				}
				if(bukkitconfig.isSet("Commands." + args[1])){
					String removing = "";
					for(int i = 2; i < args.length; i++){
						removing += args[i] + " ";
					}
					removing = removing.trim();
					try {
						List<String> newList = bukkitconfig.getStringList("Commands." + args[1]);
						if(newList.remove(removing)){
							player.sendMessage(ChatColor.GREEN + "Successfully removed '" + removing + "' from " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
						}else{
							player.sendMessage(ChatColor.RED + "Error while removing '" + removing + "' from " + ChatColor.GOLD + args[1] + ChatColor.RED + ".");
						}
						bukkitconfig.set("Commands." + args[1], newList);
						bukkitconfig.save(configfile);
					} catch (Exception e){
					}
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
							sendNoPermMsg(player);
							return true;
						}
						
						List<String> executations = bukkitconfig.getStringList("Commands." + args[0]);
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
	private boolean handleConsole(String[] args){
		try{
			if(args[0].equalsIgnoreCase("help")){
				throw new ArrayIndexOutOfBoundsException();
			}
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("[MultiCommand]: Help");
			System.out.println("[MultiCommand]: muco help    " + "Shows this page.");
			System.out.println("[MultiCommand]: muco list    " + "Listes all lists of commands.");
			System.out.println("[MultiCommand]: muco <name>    " + "Executes a list of commands.");
			System.out.println("[MultiCommand]: muco create <name>    " + "Adds a list of commands.");
			System.out.println("[MultiCommand]: muco add <name> <command>    " + "Adds a command to a list.");
			System.out.println("[MultiCommand]: muco remove <name> <command>    " + "Removes a command from a list.");
			System.out.println("[MultiCommand]: muco delete <name>    " + "Deletes a list of commands.");
			return true;
		}
		if(args[0].equalsIgnoreCase("list")){
			System.out.println("[MultiCommand]: Following command lists are set:");
			String returned = "";
			Set<String> commands = commands();
			if(commands.size() == 0){
				System.out.println("[MultiCommand]: No lists are set.");
			}else{
				for(String actCommand : commands){
					returned += actCommand + ", ";
				}
				returned = returned.substring(0, returned.length()-2);
				System.out.println("[MultiCommand]: " + returned);
			}
			return true;
		}else if(args[0].equalsIgnoreCase("delete")){
			if(args.length == 1){
				System.out.println("[MultiCommand]: Usage: /muco delete <name>");
				return true;
			}
			if(bukkitconfig.isSet("Commands." +  args[1])){
				bukkitconfig.set("Commands." + args[1], null);
				try{
					bukkitconfig.save(configfile);
				}catch(IOException e){
					System.out.println("[MultiCommand]: Error while deleting " + ChatColor.GOLD + args[1] + ChatColor.RED + "!");
				}
				System.out.println("[MultiCommand]: Successfully deleted " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
				return true;
			}else{
				System.out.println("[MultiCommand]: List " + args[1] + " doesn't exists.");
				return true;
			}
		}else if(args[0].equalsIgnoreCase("create")){
			if(args.length == 1){
				System.out.println("[MultiCommand]: Usage: /muco create <name>");
				return true;
			}
			if(bukkitconfig.isSet("Commands." + args[1])){
				System.out.println("[MultiCommand]: " +  args[1] + " already exists.");
				return true;
			}else{
				bukkitconfig.set("Commands." + args[1], new ArrayList<String>());
				try {
					bukkitconfig.save(configfile);
				} catch (IOException e) {
					System.out.println("[MultiCommand]: Failed on creating a new list!");
					e.printStackTrace();
				}
				System.out.println("[MultiCommand]: List " + args[1] + " successfully created.");
				return true;
			}
		}else if(args[0].equalsIgnoreCase("add")){
			if(args.length < 3){
				System.out.println("[MultiCommand]: Usage: /muco add <name> <command>");
				return true;
			}
			if(bukkitconfig.isSet("Commands." + args[1])){
				String adding = "";
				for(int i = 2; i < args.length; i++){
					adding += args[i] + " ";
				}
				adding = adding.trim();
				try {
					List<String> newList = bukkitconfig.getStringList("Commands." + args[1]);
					newList.add(adding);
					bukkitconfig.set("Commands." + args[1], newList);
					bukkitconfig.save(configfile);
				} catch (Exception e){
				}
				System.out.println("[MultiCommand]: Successfully added " + adding + " to " + args[1] + ".");
				return true;
			}else{
				System.out.println("[MultiCommand]: List " + args[1] + " doesn't exists.");
				return true;
			}
		}else if(args[0].equalsIgnoreCase("remove")){
			if(args.length < 3){
				System.out.println("[MultiCommand]: Usage: /muco remove <name> <command>");
				return true;
			}
			if(bukkitconfig.isSet("Commands." + args[1])){
				String removing = "";
				for(int i = 2; i < args.length; i++){
					removing += args[i] + " ";
				}
				removing = removing.trim();
				try {
					List<String> newList = bukkitconfig.getStringList("Commands." + args[1]);
					if(newList.remove(removing)){
						System.out.println("[MultiCommand]: Successfully removed '" + removing + "' from " + args[1] + ".");
					}else{
						System.out.println("[MultiCommand]: Error while removing '" + removing + "' from " + args[1] + ".");
					}
					bukkitconfig.set("Commands." + args[1], newList);
					bukkitconfig.save(configfile);
				} catch (Exception e){
				}
				return true;
			}else{
				System.out.println("[MultiCommand]: List " + args[1] + " doesn't exists.");
				return true;
			}
		}else{
			Set<String> commands = commands();
			for(String actCommand : commands){
				if(args[0].equalsIgnoreCase(actCommand)){
					List<String> executations = bukkitconfig.getStringList("Commands." + args[0]);
					for(int w = 0; w < executations.size(); w++){
						String newchatmsg = executations.toArray()[w].toString().substring(1);
						if(verbooseMode){
							System.out.println("[MultiCommand]: " + newchatmsg);
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
								System.out.println("[MultiCommand]: The command above requires at least one argument!");
								System.out.println("[MultiCommand]: Usage: /muco " + args[0] + " <arg1>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$2")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$2", args[2]);
							}catch(ArrayIndexOutOfBoundsException e){
								System.out.println("[MultiCommand]: The command above requires at least two arguments!");
								System.out.println("[MultiCommand]: Usage: /muco " + args[0] + " <arg1> <arg2>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$3")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$3", args[3]);
							}catch(ArrayIndexOutOfBoundsException e){
								System.out.println("[MultiCommand]: The command above requires at least three arguments!");
								System.out.println("[MultiCommand]: Usage: /muco " + args[0] + " <arg1> <arg2> <arg3>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$4")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$4", args[4]);
							}catch(ArrayIndexOutOfBoundsException e){
								System.out.println("[MultiCommand]: The command above requires at least four arguments!");
								System.out.println("[MultiCommand]: Usage: /muco " + args[0] + " <arg1> <arg2> <arg3> <arg4>");
								jump = true;
							}
						}
						if((Pattern.compile("\\$5")).matcher(newchatmsg).find()&&!jump){
							try{
								newchatmsg = newchatmsg.replaceAll("\\$5", args[5]);
							}catch(ArrayIndexOutOfBoundsException e){
								System.out.println("[MultiCommand]: The command above requires at least three arguments!");
								System.out.println("[MultiCommand]: Usage: /muco " + args[0] + " <arg1> <arg2> <arg3> <arg4> <arg5>");
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
			System.out.println("[MultiCommand]: List " + args[0] + " doesn't exists.");
			return true;
		}
	}
	private void sendNoPermMsg(Player player){
		if(bukkitconfig.getBoolean("Permissions"))
			player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
		else
			player.sendMessage(ChatColor.RED + "You aren't an OP!");
	}
	private void upgrade(){
		File commandsdir = new File("plugins/MultiCommand/Commands");
		File[] files = commandsdir.listFiles();
		for(int i = 0; i < files.length; i++){
			int yml = files[i].getName().length() - 4;
			String name = files[i].getName().substring(0, yml);
			Set<String> commands = YamlConfiguration.loadConfiguration(new File(commandsdir + File.separator + name + ".yml")).getKeys(false);
			bukkitconfig.set("Commands." + name, new ArrayList<String>(commands));
			files[i].delete();
		}
		commandsdir.delete();
		try{
			bukkitconfig.save(configfile);
			bukkitconfig.load(configfile);
		}catch(Exception e){}
	}
}
