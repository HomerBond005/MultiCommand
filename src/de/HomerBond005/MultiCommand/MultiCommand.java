/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.MultiCommand;

import java.io.File;
import java.io.IOException;
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
import de.HomerBond005.Permissions.PermissionsChecker;

public class MultiCommand extends JavaPlugin{
	private String mainDir = "plugins/MultiCommand";
	private File commandsdir = new File("plugins/MultiCommand/Commands");
	private File configfile = new File (mainDir + File.separator + "config.yml");
	private FileConfiguration bukkitconfig = YamlConfiguration.loadConfiguration(configfile);
	private boolean verbooseMode = false;
	private PluginManager pm;
	private CommandPre playerlistener = new CommandPre(this);
	private PermissionsChecker pc;
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
		if(!commandsdir.exists()){
			commandsdir.mkdir();
			System.out.println("[MultiCommand]: /plugins/MultiCommand/Commands created.");
		}
		System.out.println("[MultiCommand] is enabled!");
		commands();
	}
	public void onDisable(){
		System.out.println("[MultiCommand] is disabled!");
	}
	private String[] commands(){
		File[] files = commandsdir.listFiles();
		String[] CommandNames = new String[files.length];
		for(int i = 0; i < files.length; i++){
			int yml = files[i].getName().length() - 4;
			CommandNames[i] = files[i].getName().substring(0, yml);
		}
		return CommandNames;
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
		//player.sendMessage(ChatColor.RED + "/muco remove <name> <command>    " + ChatColor.YELLOW + "Removes a command from a list.");
		player.sendMessage(ChatColor.RED + "/muco delete <name>    " + ChatColor.YELLOW + "Deletes a list of commands.");
	}
	public Set<String> getShortcuts(){
		try{
			bukkitconfig.load(configfile);
		}catch(Exception e){
		}
		return bukkitconfig.getConfigurationSection("Shortcuts").getKeys(false);
	}
	public String getShortcutExe(String shortcut){
		try{
			bukkitconfig.load(configfile);
		}catch (Exception e){
		}
		return bukkitconfig.getString("Shortcuts." + shortcut);
	}
	@SuppressWarnings("unused")
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		Player player = (Player) sender;
		if(command.getName().equalsIgnoreCase("muco")){
			try{
				String test = args[0];
			}catch(ArrayIndexOutOfBoundsException e){
				help(player);
				return true;
			}
			if(args[0].equalsIgnoreCase("help")){
				help(player);
			}else if(args[0].equalsIgnoreCase("list")){
				if(!pc.has(player, "MultiCommand.list")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				player.sendMessage(ChatColor.GREEN + "Following command lists are set:");
				String returned = "";
				if(commands().length == 0){
					player.sendMessage(ChatColor.GRAY + "No lists are set.");
				}else{
					for(int e = 0; e < commands().length; e++){
						if(e + 1 == commands().length){
							returned += commands()[e];	
						}else{
							returned += commands()[e] + ", ";
						}
					}
					player.sendMessage(returned);
				}
				return true;
			}else if(args[0].equalsIgnoreCase("delete")){
				try{
					String test = args[1];
				}catch(ArrayIndexOutOfBoundsException e){
					player.sendMessage(ChatColor.RED + "Usage: /muco delete <name>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.delete." + args[1])&&!pc.has(player, "MultiCommand.delete.all")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				File f = new File(commandsdir + File.separator + args[1] + ".yml");
				if(f.exists()){
					if(f.delete() == false){
						f.delete();
						if(f.exists()){
							player.sendMessage(ChatColor.RED + "ERROR WHILE DELETING FILE!");
							player.sendMessage(ChatColor.RED + "Please delete /plugins/MultiCommand/"+ args[1] + ".yml manually.");
							return true;
						}
					}
					player.sendMessage(ChatColor.GREEN + "Successfully deleted " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + args[1] + " not set.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("create")){
				if(!pc.has(player, "MultiCommand.create." + args[1])&&!pc.has(player, "MultiCommand.create.all")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				try{
					String test = args[1];
				}catch(ArrayIndexOutOfBoundsException e){
					player.sendMessage(ChatColor.RED + "Usage: /muco create <name>");
					return true;
				}
				File f = new File(commandsdir + File.separator + args[1] + ".yml");
				if(f.exists()){
					player.sendMessage(ChatColor.RED + args[1] + " already exists.");
					return true;
				}else{
					try {
						f.createNewFile();
					} catch (IOException e) {
						player.sendMessage(ChatColor.RED + "Failed on creating new file!");
						e.printStackTrace();
					}
					player.sendMessage(ChatColor.GREEN + "List " + args[1] + " successfully created.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("add")){
				try{
					String test = args[1];
					String test2 = args[2];
				}catch(ArrayIndexOutOfBoundsException e){
					player.sendMessage(ChatColor.RED + "Usage: /muco add <name> <command>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.add." + args[1])&&!pc.has(player, "MultiCommand.add.all")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				File f = new File(commandsdir + File.separator + args[1] + ".yml");
				if(f.exists()){
					String adding = "";
					for(int i = 2; i < args.length; i++){
						adding += args[i] + " ";
					}
					FileConfiguration config = YamlConfiguration.loadConfiguration(f);
					try {
						config.set(adding, "");
						config.save(f);
					} catch (Exception e){
					}
					player.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.GOLD + adding + ChatColor.GREEN + " to " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + args[1] + " doesn't exist.");
					return true;
				}
			}else if(args[0].equalsIgnoreCase("add")){
				try{
					String test = args[1];
					String test2 = args[2];
				}catch(ArrayIndexOutOfBoundsException e){
					player.sendMessage(ChatColor.RED + "Usage: /muco add <name> <command>");
					return true;
				}
				if(!pc.has(player, "MultiCommand.add." + args[1])&&!pc.has(player, "MultiCommand.add.all")&&!pc.has(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				File f = new File(commandsdir + File.separator + args[1] + ".yml");
				if(f.exists()){
					String adding = "";
					for(int i = 2; i < args.length; i++){
						adding += args[i] + " ";
					}
					FileConfiguration config = YamlConfiguration.loadConfiguration(f);
					try {
						config.set(adding, "");
						config.save(f);
					} catch (Exception e){
					}
					player.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.GOLD + adding + ChatColor.GREEN + " to " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + args[1] + " doesn't exist.");
					return true;
				}
			}else{
				for(int i = 0; i < commands().length; i++){
					if(args[0].equalsIgnoreCase(commands()[i])){
						if(!pc.has(player, "MultiCommand.use." + args[0])&&!pc.has(player, "MultiCommand.use.all")&&!pc.has(player, "MultiCommand.all")){
							sendNoPermMsg(player);
							return true;
						}
						FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(commandsdir + File.separator + args[0] + ".yml"));
						Set<String> CommandsYML = conf.getKeys(false);
						for(int w = 0; w < CommandsYML.size(); w++){
							if(verbooseMode){
								player.sendMessage(ChatColor.DARK_RED + CommandsYML.toArray()[w].toString());
							}
							String newchatmsg = CommandsYML.toArray()[w].toString();
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
					}
				}
			}
		}
		return true;
	}
	private void sendNoPermMsg(Player player){
		if(bukkitconfig.getBoolean("Permissions"))
			player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
		else
			player.sendMessage(ChatColor.RED + "You aren't an OP!");
	}
}
