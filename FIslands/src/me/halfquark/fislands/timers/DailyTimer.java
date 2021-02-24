package me.halfquark.fislands.timers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;

import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.classes.Config;
import me.halfquark.fislands.classes.Island;

public class DailyTimer {
	
	private FIslands plugin;
	private FileConfiguration config;
	private Timer timer;
	private Config islands;
	
	public DailyTimer(){
		plugin = FIslands.instance;
		config = plugin.getConfig();
		islands = FIslands.islandsConfig;
		timer = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				/*Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Tick: " + (long)LocalDateTime.now().getHour() + "h "
							+ (long)LocalDateTime.now().getMinute() + "m "
							+ (long)LocalDateTime.now().getSecond() + "s"));*/
				
				long dayMs = ((long)LocalDateTime.now().getHour()) * 3600000L +
						((long)LocalDateTime.now().getMinute()) * 60000L + 
						((long)LocalDateTime.now().getSecond()) * 1000L;
				if(dayMs < plugin.getConfig().getLong("upkeep_check_ms")) {
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
							Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ island.region.getRegion().getId() + " does not have enough money and has fallen into ruin"));
							island.region.setMembers(null);
							island.region.setOwners(null);
							island.og = null;
							island.balance = 0.0;
						} else {
							island.balance -= getUpkeep(island.size);
						}
						//Reduce Assault Points
						Map<String, Integer> modifiedAP = new HashMap<String, Integer>();
						for(Map.Entry<String, Integer> assaultPoint : island.assaultPoints.entrySet()) {
							if(assaultPoint == null)
								continue;
							if(assaultPoint.getValue() > 1) {
								modifiedAP.put(assaultPoint.getKey(), assaultPoint.getValue() - config.getInt("assault_decay"));
							}
						}
						//Reduce Conquest Cooldown
						if(island.conquestCooldown > 0) {
							island.conquestCooldown--;
							if(island.conquestCooldown == 0) {
								island.region.getRegion().setFlag(DefaultFlag.BLOCK_BREAK, StateFlag.State.ALLOW);
								island.region.getRegion().setFlag(DefaultFlag.BLOCK_PLACE, StateFlag.State.ALLOW);
							}
						}
						newIslandList.add(island);
					}
					
					islands.set("Islands", newIslandList);
					islands.save();
				}
			};
		};
		timer.schedule(tt, new Date(), plugin.getConfig().getLong("upkeep_check_ms"));
	}
	
	private Double getUpkeep(Integer x) {
		return config.getDouble("i_upkeep_base") + x * config.getDouble("i_upkeep_size_multiplier");
	}
	
	public void stopTimer() {
		timer.cancel();
	}
	
}
