package com.bukkit.HomerBond005.MultiCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class MultiCommand extends JavaPlugin{
	String mainDir = "plugins/MultiCommand";
	File commandsdir = new File("plugins/MultiCommand/Commands");
	File configfile = new File (mainDir + File.separator + "config.yml");
	FileConfiguration bukkitconfig = YamlConfiguration.loadConfiguration(configfile);
	InputStream is = null;
	PluginManager pm;
	private CommandPre playerlistener = new CommandPre(this);
	//Permission System
	Boolean permissions = false;
	Boolean pex = false;
	PermissionManager pexmanager;
	private void setupPermissions() {
		permissions = bukkitconfig.getBoolean("Permissions", false);
		if(permissions){
			permissions = true;
			Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("PermissionsEx");
			if(permissionsPlugin != null) {
	            System.out.println("[MultiCommand]: PermissionsEx detected. Using PermissionEx.");
	            pexmanager = PermissionsEx.getPermissionManager();
	            pex = true;
	            
	        } else {
	        	System.out.println("[MultiCommand]: PermissionsEx not detected. Defaulting to BukkitPerms.");
	        	pex = false;
	        }
		}else{
			System.out.println("[MultiCommand]: Using OP-only.");
		}
    }
	//End of Permission-System
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
				bukkitconfig.save(configfile);
				System.out.println("[MultiCommand]: config.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		pm.registerEvents(playerlistener, this);
		setupPermissions();
		System.out.println("[MultiCommand]: Using Permissions: " + permissions);
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
		if(!hasPermission(player, "MultiCommand.help")){
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
				if(!hasPermission(player, "MultiCommand.list")&&!hasPermission(player, "MultiCommand.all")){
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
				if(!hasPermission(player, "MultiCommand.delete." + args[1])&&!hasPermission(player, "MultiCommand.delete.all")&&!hasPermission(player, "MultiCommand.all")){
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
				if(!hasPermission(player, "MultiCommand.create." + args[1])&&!hasPermission(player, "MultiCommand.create.all")&&!hasPermission(player, "MultiCommand.all")){
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
				if(!hasPermission(player, "MultiCommand.add." + args[1])&&!hasPermission(player, "MultiCommand.add.all")&&!hasPermission(player, "MultiCommand.all")){
					sendNoPermMsg(player);
					return true;
				}
				File f = new File(commandsdir + File.separator + args[1] + ".yml");
				if(f.exists()){
					try {
						is = new FileInputStream(f);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
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
			}
			for(int i = 0; i < commands().length; i++){
				if(args[0].equalsIgnoreCase(commands()[i])){
					if(!hasPermission(player, "MultiCommand.use." + args[0])&&!hasPermission(player, "MultiCommand.use.all")&&!hasPermission(player, "MultiCommand.all")){
						sendNoPermMsg(player);
						return true;
					}
					FileConfiguration conf = YamlConfiguration.loadConfiguration(new File(commandsdir + File.separator + args[0] + ".yml"));
					Set<String> CommandsYML = conf.getKeys(false);
					for(int w = 0; w < CommandsYML.size(); w++){
						player.sendMessage(ChatColor.DARK_RED + CommandsYML.toArray()[w].toString());
						String newchatmsg = CommandsYML.toArray()[w].toString();
						Boolean jump = false;
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
		return true;
	}
	private boolean hasPermission(Player player, String permission){
		if(permissions){
			if(pex){
				return pexmanager.has(player, permission);
			}else{
				return player.hasPermission(permission);
			}
		}else{
			return player.isOp();
		}
	}
	private void sendNoPermMsg(Player player){
		if(permissions)
			player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
		else
			player.sendMessage(ChatColor.RED + "You aren't an OP!");
	}
}
