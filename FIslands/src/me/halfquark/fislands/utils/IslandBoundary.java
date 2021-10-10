package me.halfquark.fislands.utils;

import java.util.HashSet;

import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

import me.halfquark.fislands.FIslands;

@SuppressWarnings("deprecation")
public class IslandBoundary {
	
	CuboidRegion region;
	World world;
	
	public IslandBoundary(World world, BlockVector3 c1, BlockVector3 c2) {
		c1.withY(256);
		c2.withY(256);
		region = new CuboidRegion(new BukkitWorld(world), c1, c2);
		this.world = world;
	}
	
	public IslandBoundary(World world, Location c1, Location c2) {
		region = new CuboidRegion(new BukkitWorld(world)
				, BlockVector3.at(c1.getX(), 256, c1.getZ())
				, BlockVector3.at(c2.getX(), 256, c2.getZ()));
		this.world = world;
	}
	
	public IslandBoundary(World world, ProtectedCuboidRegion pCRegion) {
		region = new CuboidRegion(new BukkitWorld(world)
				, pCRegion.getMinimumPoint().withY(256), pCRegion.getMaximumPoint().withY(256));
		this.world = world;
	}
	
	public void updateBoundary() {
		try(EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
		    HashSet<BaseBlock> blockSet = new HashSet<>();
		    blockSet.add(idToBaseBlock(FIslands.instance.getConfig().getString("boundary_block_id")));
			editSession.replaceBlocks(region, blockSet, idToBaseBlock("air"));
		    editSession.makeCuboidWalls(region, idToBaseBlock(FIslands.instance.getConfig().getString("boundary_block_id")));
		} catch (MaxChangedBlocksException ex) {
		    ex.printStackTrace();
		}
	}
	
	public void destroyBoundary() {
		try(EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(world), -1)) {
			HashSet<BaseBlock> blockSet = new HashSet<>();
		    blockSet.add(idToBaseBlock(FIslands.instance.getConfig().getString("boundary_block_id")));
			editSession.replaceBlocks(region, blockSet, idToBaseBlock("air"));
		} catch (MaxChangedBlocksException ex) {
		    ex.printStackTrace();
		}
	}

	public BaseBlock idToBaseBlock(String bId){
		return BlockTypes.get(bId).getState(ImmutableMap.of()).toBaseBlock();
	}
}
