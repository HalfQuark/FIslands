package me.halfquark.fislands.utils;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import me.halfquark.fislands.FIslands;

@SuppressWarnings("deprecation")
public class IslandBoundary {
	
	CuboidRegion region;
	World world;
	
	public IslandBoundary(World world, BlockVector c1, BlockVector c2) {
		c1.setY(256);
		c2.setY(256);
		region = new CuboidRegion(new BukkitWorld(world), c1, c2);
		this.world = world;
	}
	
	public IslandBoundary(World world, Location c1, Location c2) {
		region = new CuboidRegion(new BukkitWorld(world)
				, new com.sk89q.worldedit.BlockVector(c1.getX(), 256, c1.getZ())
				, new com.sk89q.worldedit.BlockVector(c2.getX(), 256, c2.getZ()));
		this.world = world;
	}
	
	public IslandBoundary(World world, ProtectedCuboidRegion pCRegion) {
		region = new CuboidRegion(new BukkitWorld(world)
				, pCRegion.getMinimumPoint().setY(256), pCRegion.getMaximumPoint().setY(256));
		this.world = world;
	}
	
	public void updateBoundary() {
		try {
			EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1);
		    HashSet<BaseBlock> blockSet = new HashSet<>();
		    blockSet.add(new BaseBlock(FIslands.instance.getConfig().getInt("boundary_block_id"),
		    	FIslands.instance.getConfig().getInt("boundary_block_subid")));
			editSession.replaceBlocks(region, blockSet, new BaseBlock(BlockID.AIR));
		    editSession.makeCuboidWalls(region, new BaseBlock(FIslands.instance.getConfig().getInt("boundary_block_id"),
		    	FIslands.instance.getConfig().getInt("boundary_block_subid")));
		    editSession.flushQueue();
		} catch (MaxChangedBlocksException ex) {
		    ex.printStackTrace();
		}
	}
	
	public void destroyBoundary() {
		try {
			EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1);
			HashSet<BaseBlock> blockSet = new HashSet<>();
		    blockSet.add(new BaseBlock(FIslands.instance.getConfig().getInt("boundary_block_id"),
		    	FIslands.instance.getConfig().getInt("boundary_block_subid")));
			editSession.replaceBlocks(region, blockSet, new BaseBlock(BlockID.AIR));
		    editSession.flushQueue();
		} catch (MaxChangedBlocksException ex) {
		    ex.printStackTrace();
		}
	}
}
