package me.halfquark.fislands.classes;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import org.bukkit.util.BlockVector;

@SuppressWarnings("deprecation")
public class WGRegion {
	
	public ProtectedCuboidRegion region;
	public RegionManager regionManager;
	public World world;
	
	public WGRegion(ProtectedCuboidRegion protectedCuboidRegion, World world) {
		this.region = protectedCuboidRegion;
		this.world = world;
		regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
	}
	
	public WGRegion(String id, BlockVector3 pt1, BlockVector3 pt2, World world) {
		region = new ProtectedCuboidRegion(id, pt1, pt2);
		this.world = world;
		regionManager.addRegion(region);
	}
	
	public WGRegion(String id, Location l1, Location l2, World world) {
		region = new ProtectedCuboidRegion(id,
				BlockVector3.at(l1.getX(), l1.getY(), l1.getZ()),
				BlockVector3.at(l2.getX(), l2.getY(), l2.getZ()));
		this.world = world;
		regionManager.addRegion(region);
	}
	
	public void setOwners(String[] owners) {
		region.getOwners().removeAll();
		if(owners == null)
			return;
		for(String owner : owners)
			region.getOwners().addPlayer(Bukkit.getOfflinePlayer(owner).getUniqueId());
	}
	
	public void addOwner(String owner) {
		region.getOwners().addPlayer(Bukkit.getOfflinePlayer(owner).getUniqueId());
	}
	
	public void removeOwner(String owner) {
		region.getOwners().removePlayer(Bukkit.getOfflinePlayer(owner).getUniqueId());
	}
	
	public void setMembers(String[] members) {
		region.getMembers().removeAll();
		if(members == null)
			return;
		for(String member : members)
			region.getMembers().addPlayer(Bukkit.getOfflinePlayer(member).getUniqueId());
	}
	
	public void addMember(String member) {
		region.getMembers().addPlayer(Bukkit.getOfflinePlayer(member).getUniqueId());
	}
	
	public void removeMember(String member) {
		region.getMembers().removePlayer(Bukkit.getOfflinePlayer(member).getUniqueId());
	}
	
	public ProtectedCuboidRegion getRegion() {
		return region;
	}
}
