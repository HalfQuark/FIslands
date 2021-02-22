package me.halfquark.fislands.timers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.events.AssaultWinEvent;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.warfare.assault.Assault;
import net.countercraft.movecraft.warfare.assault.AssaultManager;

public class AssaultTimer {
	
	@SuppressWarnings("unused")
	private FIslands plugin;
	@SuppressWarnings("unused")
	private FileConfiguration config;
	private AssaultManager assaultManager;
	private List<Assault> assaultList;
	private Timer timer;
	
	public AssaultTimer(FIslands plugin){
		this.plugin = plugin;
		this.config = plugin.getConfig();
		assaultManager = Movecraft.getInstance().getAssaultManager();
		timer = new Timer();
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				if(assaultList != null) {
					for(Assault oldAssault : assaultList) {
						if(!assaultManager.getAssaults().contains(oldAssault)) {
							if(oldAssault.getDamages() >= oldAssault.getMaxDamages()) {
								AssaultWinEvent assaultWinEvent = new AssaultWinEvent(oldAssault);
								Bukkit.getPluginManager().callEvent(assaultWinEvent);
							}
						}
					}
				}
				assaultList = new ArrayList<Assault>(assaultManager.getAssaults());
			};
		};
		timer.schedule(tt, new Date(), plugin.getConfig().getLong("assault_check_ms"));
	}
	
	public void stopTimer() {
		timer.cancel();
	}
	
}