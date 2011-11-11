package com.bukkit.HomerBond005.MultiCommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.yaml.snakeyaml.*;

@SuppressWarnings("deprecation")
public class MultiCommand extends JavaPlugin{
	String mainDir = "plugins/MultiCommand";
	File commandsdir = new File("plugins/MultiCommand/Commands");
	File configfile = new File (mainDir + File.separator + "config.yml");
	Configuration bukkitconfig = new Configuration(configfile);
	InputStream is = null;
	//Permission System
	Boolean PermissionsPlugin = false;
	/*private void setupPermissions() {
      Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
          if (permissionsPlugin != null) {
              System.out.println("[MultiCommand]: PermissionsBukkit detected. Using Permission system.");
              PermissionsPlugin = true;
              
          } else {
        	  System.out.println("[MultiCommand]: PermissionsBukkit not detected. Defaulting to OP.");
        	  PermissionsPlugin = false;
          }
      }*/
	//End of Permission-System
	public void onEnable(){
		if(!new File(mainDir).exists()){
			new File(mainDir).mkdir();
			System.out.println("[MultiCommand]: /plugins/MultiCommand created.");
		}
		if(!configfile.exists()){
			try{
				configfile.createNewFile();
				bukkitconfig.setProperty("Permissions", true);
				bukkitconfig.save();
				System.out.println("[MultiCommand]: config.yml created.");
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		PermissionsPlugin = bukkitconfig.getBoolean("Permissions", false);
		System.out.println("[MultiCommand]: Using Permissions: " + PermissionsPlugin);
		if(!commandsdir.exists()){
			commandsdir.mkdir();
			System.out.println("[MultiCommand]: /plugins/MultiCommand/Commands created.");
		}
		System.out.println("[MultiCommand] is enabled!");
		System.out.println();
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
		if(PermissionsPlugin){
			if(!player.hasPermission("MultiCommand.help")){
				player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
				return;
			}
		}else{
			if(!player.isOp()){
				player.sendMessage(ChatColor.RED + "You aren't an OP!");
				return;
			}
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
				if(PermissionsPlugin){
					if(!player.hasPermission("MultiCommand.list")&&!player.hasPermission("MultiCommand.all")){
						player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
						return true;
					}
				}else{
					if(!player.isOp()){
						player.sendMessage(ChatColor.RED + "You aren't an OP!");
						return true;
					}
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
				if(PermissionsPlugin){
					if(!player.hasPermission("MultiCommand.delete." + args[1])&&!player.hasPermission("MultiCommand.delete.all")&&!!player.hasPermission("MultiCommand.all")){
						player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
						return true;
					}
				}else{
					if(!player.isOp()){
						player.sendMessage(ChatColor.RED + "You aren't an OP!");
						return true;
					}
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
				if(PermissionsPlugin){
					if(!player.hasPermission("MultiCommand.create." + args[1])&&!player.hasPermission("MultiCommand.create.all")&&!!player.hasPermission("MultiCommand.all")){
						player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
						return true;
					}
				}else{
					if(!player.isOp()){
						player.sendMessage(ChatColor.RED + "You aren't an OP!");
						return true;
					}
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
				if(PermissionsPlugin){
					if(!player.hasPermission("MultiCommand.add." + args[1])&&!player.hasPermission("MultiCommand.add.all")&&!!player.hasPermission("MultiCommand.all")){
						player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
						return true;
					}
				}else{
					if(!player.isOp()){
						player.sendMessage(ChatColor.RED + "You aren't an OP!");
						return true;
					}
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
					Configuration config = new Configuration(f);
					config.load();
					config.setProperty(adding, "");
					config.save();
					player.sendMessage(ChatColor.GREEN + "Successfully added " + ChatColor.GOLD + adding + ChatColor.GREEN + " to " + ChatColor.GOLD + args[1] + ChatColor.GREEN + ".");
					return true;
				}else{
					player.sendMessage(ChatColor.RED + "List " + args[1] + " doesn't exist.");
					return true;
				}
			}
			for(int i = 0; i < commands().length; i++){
				if(args[0].equalsIgnoreCase(commands()[i])){
					if(PermissionsPlugin){
						if(!player.hasPermission("MultiCommand.use." + args[1])&&!player.hasPermission("MultiCommand.use.all")&&!!player.hasPermission("MultiCommand.all")){
							player.sendMessage(ChatColor.RED + "You don't have the permission to use this!");
							return true;
						}
					}else{
						if(!player.isOp()){
							player.sendMessage(ChatColor.RED + "You aren't an OP!");
							return true;
						}
					}
					try {
						is = new FileInputStream(commandsdir + File.separator + args[0] + ".yml");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					Yaml yaml = new Yaml();
					@SuppressWarnings("unchecked")
					Map<Object, Object> CommandsYML = (Map<Object, Object>)yaml.load(is);
					for(int w = 0; w < CommandsYML.size(); w++){
						player.sendMessage(ChatColor.DARK_RED + CommandsYML.keySet().toArray()[w].toString());
						String newchatmsg = CommandsYML.keySet().toArray()[w].toString();
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
}
