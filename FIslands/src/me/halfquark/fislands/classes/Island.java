package me.halfquark.fislands.classes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class Island implements ConfigurationSerializable {
	
	public WGRegion region;
	public UUID og;
	public Double balance;
	public Integer size;
	public HashMap<String, Integer> assaultPoints;
	public Integer conquestCooldown;
	
	public Island(WGRegion region, UUID og, Double balance, Integer size) {
		this.region = region;
		this.og = og;
		this.balance = balance;
		this.size = size;
		this.assaultPoints = new HashMap<String, Integer>();
		this.conquestCooldown = 0;
	}
	
	@SuppressWarnings("unchecked")
	public Island(Map<String, Object> serializedIsland) {
		World world = Bukkit.getWorld((String)serializedIsland.get("world"));
		this.region = new WGRegion((ProtectedCuboidRegion)WorldGuardPlugin.inst().getRegionManager(world).getRegion((String)serializedIsland.get("regionId")),
				world);
    	this.og = UUID.fromString((String) serializedIsland.get("og"));
    	this.balance = (Double) serializedIsland.get("balance");
    	this.size = (Integer) serializedIsland.get("size");
    	this.assaultPoints = (HashMap<String, Integer>) serializedIsland.get("assaultPoints");
    	this.conquestCooldown = (Integer) serializedIsland.get("conquestCooldown");
	}
    
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("world", region.world.getName());
        result.put("regionId", region.getRegion().getId());
        result.put("og", og.toString());
        result.put("balance", balance);
        result.put("size", size);
        result.put("assaultPoints", assaultPoints);
        result.put("conquestCooldown", conquestCooldown);
        return result;
    }
    
    public void expand(Integer x) {
    	this.region.getRegion().setMinimumPoint(this.region.getRegion().getMinimumPoint().add(-1*x, 0, -1*x).toBlockVector());
    	this.region.getRegion().setMaximumPoint(this.region.getRegion().getMaximumPoint().add(x, 0, x).toBlockVector());
    	size += x;
    	return;
    }
    
    public void destroy() {
    	this.region.regionManager.removeRegion(this.region.getRegion().getId());
    	this.region = null;
    }
    
}
