package me.halfquark.fislands.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sk89q.worldguard.protection.flags.Flags;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.protection.flags.StateFlag;

import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.classes.Config;
import me.halfquark.fislands.classes.Island;
import net.countercraft.movecraft.warfare.assault.Assault;

public class AssaultEventListener implements Listener {
	
	FIslands plugin;
	Config islands;
	FileConfiguration config;
	
	public AssaultEventListener() {
		plugin = FIslands.instance;
		islands = FIslands.islandsConfig;
		config = plugin.getConfig();
	}
	
	@EventHandler
	public void onAssaultWin(final AssaultWinEvent assaultEvent) {
		Assault assault = assaultEvent.getAssault();
		assault.getStarterUUID();
		islands.reload();
		@SuppressWarnings("unchecked")
		List<Island> islandList = (List<Island>) islands.getList("Islands");
		List<Island> newIslandList = new ArrayList<Island>();
		for(Island island : islandList) {
			if(island.region.getRegion().getId().equals(assault.getRegionName())) {
				Integer APs = 0;
				if(island.assaultPoints == null) {
					island.assaultPoints = new HashMap<String, Integer>();
				}
				if(island.assaultPoints.get(assault.getStarterUUID().toString()) != null) {
					APs = island.assaultPoints.get(assault.getStarterUUID().toString());
				}
				APs += config.getInt("assault_reward");
				island.assaultPoints.put(assault.getStarterUUID().toString(), APs);
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ Bukkit.getPlayer(assault.getStarterUUID()).getDisplayName() + " has successfully assaulted " + assault.getRegionName()
						+ " and is now holding " + APs + "/" + config.getInt("assault_cap") + " Assault Points"));
				if(APs >= config.getInt("assault_cap")) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ Bukkit.getPlayer(assault.getStarterUUID()).getDisplayName() + " has successfully conquered " + assault.getRegionName()));
					island.og = assault.getStarterUUID();
					if(island.region == null) {
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:AssaultWinNullRegion"));
					} else {
					island.region.setOwners(null);
					island.region.addOwner(Bukkit.getPlayer(assault.getStarterUUID()).getName());
					island.region.setMembers(null);
					island.region.getRegion().setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
					island.region.getRegion().setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
					}
					island.conquestCooldown = config.getInt("conquest_cooldown");
					island.assaultPoints = null;
				}
			}
			newIslandList.add(island);
		}
		islands.set("Islands", newIslandList);
		islands.save();
	}
}
