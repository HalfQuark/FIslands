package me.halfquark.fislands.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.Material;

import me.halfquark.fislands.classes.Pair;

public class BlockQuery {
	
	HashSet<Location> Visited;
	Queue<Pair<Location, Integer>> BlockQueue;
	
	public BlockQuery() {
		Visited = new HashSet<Location>();
		BlockQueue = new LinkedList<Pair<Location, Integer> >();
	}
	
	public Location QueryRadius (Location center, Material target, Integer radius) {
		center = center.getBlock().getLocation();
		BlockQueue.add(new Pair<>(center, radius));
		Pair<Location, Integer> head;
		while(!BlockQueue.isEmpty()) {
			head = BlockQueue.remove();
			if(Visited.contains(head.F))
				continue;
			Visited.add(head.F);
			if(head.F.getBlock().getType() == target)
				return head.F;
			if(head.S <= 0)
				continue;
			BlockQueue.add(new Pair<>(head.F.clone().add(1, 0, 0), head.S - 1));
			BlockQueue.add(new Pair<>(head.F.clone().add(-1, 0, 0), head.S - 1));
			BlockQueue.add(new Pair<>(head.F.clone().add(0, 1, 0), head.S - 1));
			BlockQueue.add(new Pair<>(head.F.clone().add(0, -1, 0), head.S - 1));
			BlockQueue.add(new Pair<>(head.F.clone().add(0, 0, 1), head.S - 1));
			BlockQueue.add(new Pair<>(head.F.clone().add(0, 0, -1), head.S - 1));
			//head.F.getBlock().setType(Material.STAINED_GLASS);
		}
		return null;
	}
	
}
