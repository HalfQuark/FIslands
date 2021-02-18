package me.halfquark.fislands.events;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.classes.Config;
import me.halfquark.fislands.classes.Island;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.events.AssaultWinEvent;

public class AssaultEventListener implements Listener {
	
	FIslands plugin;
	Config islands;
	FileConfiguration config;
	
	public AssaultEventListener(FIslands plugin) {
		this.plugin = plugin;
		islands = new Config("islands.yml");
		config = plugin.getConfig();
	}
	
	@EventHandler
	public void onAssaultWin(final AssaultWinEvent assaultEvent) {
		Assault assault = assaultEvent.getAssault();
		assault.getStarterUUID();
		@SuppressWarnings("unchecked")
		List<Island> islandList = (List<Island>) islands.getList("Islands");
		List<Island> newIslandList = new ArrayList<Island>();
		for(Island island : islandList) {
			if(island.region.getRegion().getId().equals(assault.getRegionName())) {
				Integer APs = 0;
				if(island.assaultPoints.get(assault.getStarterUUID().toString()) != null) {
					APs = island.assaultPoints.get(assault.getStarterUUID().toString());
				}
				APs += config.getInt("assault_reward");
				island.assaultPoints.put(assault.getStarterUUID().toString(), APs);
				Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ Bukkit.getPlayer(assault.getStarterUUID()).getDisplayName() + " has successfully assaulted " + assault.getRegionName()
						+ "and is now holding " + APs + "/" + config.getInt("assault_cap") + " Assault Points"));
				if(APs >= config.getInt("assault_cap")) {
					Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ Bukkit.getPlayer(assault.getStarterUUID()).getDisplayName() + " has successfully conquered " + assault.getRegionName()));
					island.og = assault.getStarterUUID();
					island.region.setOwners(null);
					island.region.addOwner(Bukkit.getPlayer(assault.getStarterUUID()).getName());
					island.region.setMembers(null);
					island.region.getRegion().setFlag(DefaultFlag.BLOCK_BREAK, StateFlag.State.DENY);
					island.region.getRegion().setFlag(DefaultFlag.BLOCK_PLACE, StateFlag.State.DENY);
					island.conquestCooldown = config.getInt("conquest_cooldown");
				}
			}
			newIslandList.add(island);
		}
		islands.set("Islands", newIslandList);
		islands.save();
	}
}
