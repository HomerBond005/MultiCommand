/*
 * Copyright HomerBond005
 * 
 *  Published under CC BY-NC-ND 3.0
 *  http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package de.HomerBond005.MultiCommand;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import de.bananaco.bpermissions.api.ApiLayer;
import de.bananaco.bpermissions.api.util.CalculableType;

/**
 * Class to check permission nodes. Supports: Vault, PermissionsEx, bPermissions, GroupManager and BukkitPermissions
 * @version v1.1
 * @author HomerBond005
 */
public class PermissionsChecker{
	int permSys;
	PermissionManager pexmanager;
    PermissionsPlugin pbplugin;
    GroupManager gm;
    Permission vault;
    boolean usePerm;
    Logger log;
    public PermissionsChecker(Plugin main, boolean usePerm){
    	this.usePerm = usePerm;
    	this.log = main.getLogger();
    	setupPermissions();
    }
    private void setupPermissions(){
    	if(usePerm){
    		PluginManager pm = Bukkit.getPluginManager();
    		if(pm.getPlugin("Vault") != null){
	    		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
				if (permissionProvider != null) {
		            vault = permissionProvider.getProvider();
		            log.log(Level.INFO, "Using Vault!");
		            permSys = 5;
		        }
    		}else if(pm.getPlugin("PermissionsEx") != null){
    			log.log(Level.INFO, "Using PermissionsEx!");
    			pexmanager = PermissionsEx.getPermissionManager();
    			permSys = 2;
    		}else if(pm.getPlugin("bPermissions") != null){
    			log.log(Level.INFO, "Using bPermissions!");
    			permSys = 3;
    		}else if(pm.getPlugin("GroupManager") != null){
    			log.log(Level.INFO, "Using GroupManager!");
    			gm = (GroupManager)pm.getPlugin("GroupManager");
    			permSys = 4;
    		}else{
    			log.log(Level.INFO, "Using Bukkit Permissions!");
    			permSys = 1;
    		}
    	}else{
    		log.log(Level.INFO, "Using OP-only!");
    		permSys = 0;
    	}
    }
    public boolean has(Player player, String perm){
    	if(permSys == 0){
    		return player.isOp();
    	}else if(permSys == 1){
    		return player.hasPermission(perm);
    	}else if(permSys == 2){
    		return pexmanager.has(player, perm);
    	}else if(permSys == 3){
    		return ApiLayer.hasPermission(player.getWorld().getName(), CalculableType.USER, player.getName(), perm);
    	}else if(permSys == 4){
    		AnjoPermissionsHandler handler = gm.getWorldsHolder().getWorldPermissions(player.getName());
    		if (handler == null)
                return false;
            return handler.permission(player.getName(), perm);
    	}else if(permSys == 5){
    		return vault.has(player, perm);
    	}else{
    		return false;
    	}
    }
    public void sendNoPermMsg(Player player){
    	if(permSys == 0){
    		player.sendMessage(ChatColor.RED + "You have to be OP to use this command!");
    	}else{
    		player.sendMessage(ChatColor.RED + "You don't have the necessary permission!");
    	}
    }
}
