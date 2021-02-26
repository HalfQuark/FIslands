package me.halfquark.fislands.commands;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.RegionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.classes.Config;
import me.halfquark.fislands.classes.Faction;
import me.halfquark.fislands.classes.Island;
import me.halfquark.fislands.classes.WGRegion;
import me.halfquark.fislands.utilities.BlockQuery;
import me.halfquark.fislands.utilities.IslandBoundary;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class IsCommandEx implements CommandExecutor{
	
	FIslands plugin;
	FileConfiguration config;
	Economy economy;
	Config islands;
	RegionContainer regContainer = WorldGuardPlugin.inst().getRegionContainer();
	RegionQuery regQuery = regContainer.createQuery();
	ApplicableRegionSet set;
	List<Island> islandList;
	IslandBoundary islandBoundary;
	
	public IsCommandEx() {
		plugin = FIslands.instance;
		config = plugin.getConfig();
		economy = plugin.getEconomy();
		if(economy == null) {
			Bukkit.getLogger().log(Level.SEVERE, "[FIslands] Economy not passed to IsCommandEx");
        }
		islands = FIslands.islandsConfig;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(config.getString("msg_prefix") + config.getString("msg_accent")
					+ "You must be a player to execute this command!");
			return false;
		}
		Player pSender = (Player) sender;
		if(!cmd.getName().equalsIgnoreCase("island"))
			return false;
		if(args.length < 1)
			return help(args, pSender);
		switch(args[0]) {
		case "claim":
			if(args.length != 2)
				return help(args, pSender);
			BlockQuery bq = new BlockQuery();
			Location coreLoc = bq.QueryRadius(pSender.getLocation(), Material.getMaterial(config.getString("i_core_block")), config.getInt("i_creation_radius"));
			if(coreLoc == null) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "You must be within " + config.getString("i_creation_radius")
						+ " blocks of an island core [" + config.getString("i_core_block") + "]"));
				return true;
			}
			if(economy.getBalance(pSender) < config.getDouble("i_creation_price")) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "You do not have enough balance to claim an island. "
					+ "You need at least " + config.getDouble("i_creation_price") + "$"));
				return true;
			}
			if(WorldGuardPlugin.inst().getRegionManager(pSender.getWorld()).getRegion(args[1].toLowerCase()) != null) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is already a region with this name"));
				return true;
			}
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "There is already an Island with this name"));
						return true;
					}
				}
			} else {
				islandList = new ArrayList<>();
			}
			com.sk89q.worldedit.BlockVector corner1 = new com.sk89q.worldedit.BlockVector(coreLoc.getX() + config.getInt("i_creation_bound_x1")
																					, 0
																					, coreLoc.getZ() + config.getInt("i_creation_bound_z1"));
			com.sk89q.worldedit.BlockVector corner2 = new com.sk89q.worldedit.BlockVector(coreLoc.getX() + config.getInt("i_creation_bound_x2")
																					, 256
																					, coreLoc.getZ() + config.getInt("i_creation_bound_z2"));
			ProtectedCuboidRegion testRegion = new ProtectedCuboidRegion("dummy", corner1, corner2);
			RegionManager regManager = regContainer.get(pSender.getWorld());
			try {
				ApplicableRegionSet set = regManager.getApplicableRegions(testRegion);
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
			if(set.size() > config.getInt("i_creation_overlap_regions")) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "The island region intersects with claimed regions"));
				return true;
			}
			WGRegion islandRegion = new WGRegion(
					args[1],
					corner1,
					corner2,
					pSender.getWorld());
			islandRegion.setOwners(null);
			islandRegion.addOwner(pSender.getName());
			Island newIsland = new Island(islandRegion, pSender.getUniqueId(), config.getDouble("i_creation_bal"), config.getInt("i_creation_size"));
			newIsland.region.getRegion().setFlag(DefaultFlag.TNT, StateFlag.State.DENY);
			islandList.add(newIsland);
			economy.withdrawPlayer(pSender, config.getDouble("i_creation_price"));
			islands.set("Islands", islandList);
			islands.save();
			islandBoundary = new IslandBoundary(pSender.getWorld(), newIsland.region.getRegion());
			islandBoundary.updateBoundary();
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
					+ config.getString("msg_primary")+ "Island claimed successfully!"));
			return true;
		case "info":
			if(args.length > 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList == null || islandList.size() == 0) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
						+ config.getString("msg_accent")+ "There are no islands on this server"));
				break;
			}
			String isName;
			if(args.length == 1) {
				ApplicableRegionSet regionSet = regQuery.getApplicableRegions(pSender.getLocation());
				if(regionSet.size() > 1) {
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
							+ config.getString("msg_accent")+ "You are standing in multiple regions. Please specify one of the following:"));
					for(ProtectedRegion region : regionSet) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
								+ config.getString("msg_primary") + "  " + region.getId()));
					}
					break;
				}
				if(regionSet.size() == 0) {
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
							+ config.getString("msg_accent")+ "There is no island in this position"));
					break;
				}
				isName = regionSet.getRegions().iterator().next().getId();
			} else {
				isName = args[1];
			}
			for(Island island : islandList) {
				if(island == null)
					continue;
				if(island.region == null) {
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
					continue;
				}
				if(island.region.getRegion().getId().equalsIgnoreCase(isName)) {
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
							+ config.getString("msg_accent") + island.region.getRegion().getId()));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
							+ "  *" + Bukkit.getOfflinePlayer(island.og).getName()));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
							+ "  Owners: " + island.region.getRegion().getOwners().getPlayers().toString()));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
							+ "  Members: " + island.region.getRegion().getMembers().getPlayers().toString()));
					Faction ogFaction = Faction.fromPlayer(island.og);
					if(ogFaction != null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
								+ "  Faction: " + ogFaction.name));
					}
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")
							+ "  Size: " + island.size));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")
							+ "  Balance: " + island.balance + "$"));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")
							+ "  Upkeep: " + getUpkeep(island.size) + "$"));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")
							+ "  Days remaining: " + (int)(island.balance / getUpkeep(island.size))));
					if(island.assaultPoints != null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
								+ "  Assault points: "));
						for(Map.Entry<String, Integer> mapEntry : island.assaultPoints.entrySet()) {
							if(mapEntry == null)
								continue;
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
									+ "   " + Bukkit.getOfflinePlayer(UUID.fromString(mapEntry.getKey())).getName() + ": " + mapEntry.getValue()));
						}
					}
					return true;
				}
			}
			if(args.length == 1) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
						+ config.getString("msg_accent")+ "There is no island in this position"));
			} else {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
						+ config.getString("msg_accent") + "There is no island with this name"));
				return true;
			}
			break;
		case "list":
			int page;
			if(args.length <= 1) {
				page = 1;
			}else {
				try {
					page = Integer.parseInt(args[1]);
				} catch(NumberFormatException e) {
					page = 1;
				}
			}
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList == null || islandList.size() == 0) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
						+ config.getString("msg_accent")+ "There are no islands on this server"));
				return true;
			}
			if(page > islandList.size() / 10 + 1)
				page = islandList.size() / 10 + 1;
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
					+ config.getString("msg_accent") + "Island list " + config.getString("msg_primary") + "[page:" + page + "/" + (islandList.size() / 10 + 1) + "]"));
			char identifier;
			for(int i = 10 * (page - 1); i < islandList.size() && i < 10* page; i++) {
				if(islandList.get(i) == null)
					continue;
				if(islandList.get(i).region == null) {
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
					continue;
				}
				identifier = ' ';
				if(islandList.get(i).region.getRegion().getMembers().contains(WorldGuardPlugin.inst().wrapPlayer(pSender)))
					identifier = '+';
				if(islandList.get(i).region.getRegion().getOwners().contains(WorldGuardPlugin.inst().wrapPlayer(pSender)))
					identifier = '»';
				if(islandList.get(i).og.equals(pSender.getUniqueId()))
					identifier = '*';
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent") + identifier
					+ config.getString("msg_primary") + islandList.get(i).region.getRegion().getId()));
			}
			return true;
		case "unclaim":
			if(args.length != 2)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.og.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Island's og"));
							return true;
						}
						economy.depositPlayer(pSender, island.balance);
						RegionManager regionManager = WorldGuardPlugin.inst().getRegionManager(pSender.getWorld());
						regionManager.removeRegion(island.region.getRegion().getId());
						islandBoundary = new IslandBoundary(pSender.getWorld(), island.region.getRegion());
						islandBoundary.destroyBoundary();
						islandList.remove(island);
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Island unclaimed successfully"));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "transfer":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.og.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Island's og"));
							return true;
						}
						OfflinePlayer newOg = Bukkit.getOfflinePlayer(args[2]);
						if(!newOg.hasPlayedBefore()) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player hasn't joined the server"));
							return true;
						}
						island.og = newOg.getUniqueId();
						island.region.addOwner(newOg.getName());
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Island transfered successfully"));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "deposit":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.region.getRegion().getOwners().contains(WorldGuardPlugin.inst().wrapPlayer(pSender))
								&& !island.region.getRegion().getMembers().contains(WorldGuardPlugin.inst().wrapPlayer(pSender))) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not added to this Island"));
							return true;
						}
						Double money;
						try {
							money = Double.parseDouble(args[2]);
						}catch(NumberFormatException e) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "Please specify a valid number"));
							return true;
						}
						if(economy.getBalance(pSender) < money) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You don't have enough money"));
							return true;
						}
						economy.withdrawPlayer(pSender, money);
						island.balance += money;
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You deposited " + money + "$ in " + island.region.getRegion().getId()));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "withdraw":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.region.getRegion().getOwners().contains(WorldGuardPlugin.inst().wrapPlayer(pSender))) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not an owner of this Island"));
							return true;
						}
						Double money;
						try {
							money = Double.parseDouble(args[2]);
						}catch(NumberFormatException e) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "Please specify a valid number"));
							return true;
						}
						if(island.balance < money) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "The Island does not have enough money"));
							return true;
						}
						economy.depositPlayer(pSender, money);
						island.balance -= money;
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You withdrew " + money + "$ from " + island.region.getRegion().getId()));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "addmember":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.region.getRegion().getOwners().contains(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not an owner of this Island"));
							return true;
						}
						OfflinePlayer newMember = Bukkit.getOfflinePlayer(args[2]);
						if(!newMember.hasPlayedBefore()) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player hasn't joined the server"));
							return true;
						}
						island.region.addMember(newMember.getName());
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Member added successfully"));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "removemember":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.region.getRegion().getOwners().contains(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not an owner of this Island"));
							return true;
						}
						OfflinePlayer member = Bukkit.getOfflinePlayer(args[2]);
						if(!member.hasPlayedBefore()) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player hasn't joined the server"));
							return true;
						}
						if(!island.region.getRegion().getMembers().contains(WorldGuardPlugin.inst().wrapOfflinePlayer(member))) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player isn't an Island's member"));
							return true;
						}
						island.region.removeMember(member.getName());
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Member removed successfully"));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "addowner":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.og.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Island's og"));
							return true;
						}
						OfflinePlayer newOwner = Bukkit.getOfflinePlayer(args[2]);
						if(!newOwner.hasPlayedBefore()) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player hasn't joined the server"));
							return true;
						}
						island.region.addOwner(newOwner.getName());
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Owner added successfully"));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "removeowner":
			if(args.length != 3)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.og.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Island's og"));
							return true;
						}
						OfflinePlayer owner = Bukkit.getOfflinePlayer(args[2]);
						if(!owner.hasPlayedBefore()) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player hasn't joined the server"));
							return true;
						}
						if(!island.region.getRegion().getOwners().contains(WorldGuardPlugin.inst().wrapOfflinePlayer(owner))) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player isn't an Island's owner"));
							return true;
						}
						island.region.removeOwner(owner.getName());
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Owner removed successfully"));
						islands.set("Islands", islandList);
						islands.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		case "expand":
			if(args.length != 2)
				return help(args, pSender);
			islands.reload();
			islandList = (List<Island>) islands.getList("Islands");
			if(islandList != null && islandList.size() != 0) {
				for(Island island : islandList) {
					if(island == null)
						continue;
					if(island.region == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Something has gone terribly wrong. Please contact an admin. Error:IslandNullRegion"));
						continue;
					}
					if(island.region.getRegion().getId().equalsIgnoreCase(args[1])) {
						if(!island.og.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Island's og"));
							return true;
						}
						if(island.balance < config.getInt("i_expansion_price")) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "Not enough Island's balance. You need " + config.getDouble("i_expansion_price") + "$"));
							return true;
						}
						if(island.size + config.getInt("i_expansion_size") > config.getInt("i_expansion_limit")) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This island has reached the maximum size. You cannot expand it further"));
							return true;
						}
						island.balance -= config.getDouble("i_expansion_price");
						island.size += config.getInt("i_expansion_size");
						com.sk89q.worldedit.BlockVector bv1 = new com.sk89q.worldedit.BlockVector(island.region.getRegion().getMinimumPoint().getX() - config.getInt("i_expansion_size")
																								, island.region.getRegion().getMinimumPoint().getY()
																								, island.region.getRegion().getMinimumPoint().getZ() - config.getInt("i_expansion_size"));
						com.sk89q.worldedit.BlockVector bv2 = new com.sk89q.worldedit.BlockVector(island.region.getRegion().getMaximumPoint().getX() + config.getInt("i_expansion_size")
																								, island.region.getRegion().getMaximumPoint().getY()
																								, island.region.getRegion().getMaximumPoint().getZ() + config.getInt("i_expansion_size"));
						ProtectedCuboidRegion newRegion = new ProtectedCuboidRegion("dummy", bv1, bv2);
						regManager = regContainer.get(pSender.getWorld());
						try {
							set = regManager.getApplicableRegions(newRegion);
						} catch(NullPointerException e) {
							e.printStackTrace();
						}
						if(set.size() > config.getInt("i_creation_overlap_regions") + 1) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "The expansion intersects with claimed regions"));
							return true;
						}
						island.region.getRegion().setMinimumPoint(bv1);
						island.region.getRegion().setMaximumPoint(bv2);
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Island expanded by " + config.getInt("i_expansion_size") + " blocks"));
						islands.set("Islands", islandList);
						islands.save();
						islandBoundary = new IslandBoundary(pSender.getWorld(), island.region.getRegion());
						islandBoundary.updateBoundary();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Island with this name"));
			break;
		}
		return help(args, pSender);
	}
	
	private boolean help(String[] args, Player player) {
		if(args.length > 0) {
			switch(args[0]) {
			case "claim":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is claim <island name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Claim an Island."));
				return true;
			case "info":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is info [island name]"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Display island info."));
				return true;
			case "list":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is list [page]"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " List all Islands on this world."));
				return true;
			case "unclaim":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is unclaim <island name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Unclaim an Island."));
				return true;
			case "transfer":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is transfer <island name> <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Transfer ownership of an Island."));
				return true;
			case "deposit":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is deposit <island name> <ammount>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Deposit money on Island's balance."));
				return true;
			case "withdraw":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is withdraw <island name> <ammount>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Withdraw money from Island's balance."));
				return true;
			case "addmember":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is addmember <island name> <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Add a member to the Island."));
				return true;
			case "removemember":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is removemember <island name> <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Remove a member from the Island."));
				return true;
			case "addowner":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is addowner <island name> <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Add a owner to the Island."));
				return true;
			case "removeowner":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is removeowner <island name> <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Remove a owner from the Island."));
				return true;
			case "expand":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/is expand <island name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Expand an Island."));
				return true;
			}
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
			+ "/is"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " claim"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " info"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " list"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " unclaim"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " transfer"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " deposit"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " withdraw"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " addmember"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " removemember"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " addowner"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " removeowner"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " expand"));
		return true;
	}
	
	private Double getUpkeep(Integer x) {
		return config.getDouble("i_upkeep_base") + x*config.getDouble("i_upkeep_size_multiplier");
	}
	
}
