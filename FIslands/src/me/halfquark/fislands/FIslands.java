package me.halfquark.fislands;

import java.io.File;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.halfquark.fislands.classes.Faction;
import me.halfquark.fislands.classes.Island;
import me.halfquark.fislands.classes.Member;
import me.halfquark.fislands.classes.Rank;
import me.halfquark.fislands.commands.FCommandEx;
import me.halfquark.fislands.commands.IsCommandEx;
import me.halfquark.fislands.events.AssaultEventListener;
import me.halfquark.fislands.timers.DailyTimer;
import net.milkbowl.vault.economy.Economy;

public class FIslands extends JavaPlugin {
	
	public static FIslands instance;
	public File islandsFile = new File(getDataFolder(), "islands.yml");
	public FileConfiguration islands = YamlConfiguration.loadConfiguration(islandsFile);
	public Economy economy;
	public DailyTimer dailyTimer;
	
	public void onEnable()
	{
		instance = this;
		ConfigurationSerialization.registerClass(Island.class);
		ConfigurationSerialization.registerClass(Faction.class);
		ConfigurationSerialization.registerClass(Member.class);
		ConfigurationSerialization.registerClass(Rank.class);
		if(!setupEconomy()) {
			Bukkit.getLogger().log(Level.SEVERE, "[FIslands] Economy not found.");
        }
		getCommand("island").setExecutor(new IsCommandEx(this));
		getCommand("faction").setExecutor(new FCommandEx(this));
		this.saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		dailyTimer = new DailyTimer(this);
		Bukkit.getPluginManager().registerEvents(new AssaultEventListener(this), this);
	}
	
	public void onDisable()
	{
		
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	public DailyTimer getIsUpkTimer() {
		return dailyTimer;
	}
	
	public FIslands getInstance() {
		return instance;
	}
	
}
