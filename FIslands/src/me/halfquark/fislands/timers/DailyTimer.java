package me.halfquark.fislands.timers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;

import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.classes.Config;
import me.halfquark.fislands.classes.Island;
import me.halfquark.fislands.utils.IslandBoundary;

public class DailyTimer {
	
	private FIslands plugin;
	private FileConfiguration config;
	//private Timer timer;
	private Config islands;
	private Config upkeep;
	private boolean pastMidDay;
	
	public DailyTimer(){
		plugin = FIslands.instance;
		config = plugin.getConfig();
		islands = FIslands.islandsConfig;
		upkeep = FIslands.upkeepConfig;
		//timer = new Timer();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
		     @Override
		     public void run() {
		     	task();
		     }
		}, 0L, plugin.getConfig().getLong("upkeep_check_ms") / 50L);
		
		/*TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				task();
			};
		};
		timer.schedule(tt, new Date(), plugin.getConfig().getLong("upkeep_check_ms"));*/
	}
	
	private void task() {
		//Bukkit.broadcastMessage("Tick");
		upkeep.reload();
		if(!upkeep.contains("pastMidDay")) {
			upkeep.set("pastMidDay", LocalDateTime.now().getHour() > 12);
			upkeep.save();
		}
		pastMidDay = upkeep.getBoolean("pastMidDay");
		if(pastMidDay && LocalDateTime.now().getHour() < 12) {
			pastMidDay = false;
		}
		if(!pastMidDay && LocalDateTime.now().getHour() >= 12) {
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "[A day has passed and Island upkeeps have been charged]"));
			islands.reload();
			@SuppressWarnings("unchecked")
			List<Island> islandList = (List<Island>) islands.getList("Islands");
			List<Island> newIslandList = new ArrayList<Island>();
			if(islandList == null || islandList.size() == 0) {
				return;
			}
			for(Island island : islandList) {
				if(island == null)
					continue;
				if(island.balance < getUpkeep(island.size)) {
					//if(!island.og.equals(new UUID(0L, 0L))) {
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ island.region.getRegion().getId() + " does not have enough money and has fallen into ruin"));
						IslandBoundary islandBoundary = new IslandBoundary(island.region.world, island.region.getRegion());
						islandBoundary.destroyBoundary();
						RegionContainer regContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
						RegionManager regManager = regContainer.get(BukkitAdapter.adapt(island.region.world));
						regManager.removeRegion(island.region.getRegion().getId());
						continue;
						//FIslands.instance.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(island.og), island.balance);
						/*island.region.setMembers(null);
						island.region.setOwners(null);
						island.balance = 0D;
						island.og = new UUID(0L, 0L);*/
					//}
				} else {
					island.balance -= getUpkeep(island.size);
				}
				//Reduce Assault Points
				if(island.assaultPoints != null) {
					Map<String, Integer> modifiedAP = new HashMap<String, Integer>();
					for(Map.Entry<String, Integer> assaultPoint : island.assaultPoints.entrySet()) {
						if(assaultPoint == null)
							continue;
						if(assaultPoint.getValue() > 1) {
							modifiedAP.put(assaultPoint.getKey(), assaultPoint.getValue() - config.getInt("assault_decay"));
						}
					}
				}
				//Reduce Conquest Cooldown
				if(island.conquestCooldown > 0) {
					island.conquestCooldown--;
					if(island.conquestCooldown == 0) {
						island.region.getRegion().setFlag(Flags.BLOCK_BREAK, StateFlag.State.ALLOW);
						island.region.getRegion().setFlag(Flags.BLOCK_PLACE, StateFlag.State.ALLOW);
					}
				}
				newIslandList.add(island);
			}
			
			islands.set("Islands", newIslandList);
			islands.save();
			pastMidDay = true;
		}
		upkeep.set("pastMidDay", pastMidDay);
		upkeep.save();
	}
	
	private Double getUpkeep(Integer x) {
		return config.getDouble("i_upkeep_base") + x * config.getDouble("i_upkeep_size_multiplier");
	}
	
}
