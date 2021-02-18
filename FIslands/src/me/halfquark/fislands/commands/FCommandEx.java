package me.halfquark.fislands.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sk89q.worldguard.protection.ApplicableRegionSet;

import me.halfquark.fislands.FIslands;
import me.halfquark.fislands.classes.Config;
import me.halfquark.fislands.classes.Faction;
import me.halfquark.fislands.classes.Member;
import me.halfquark.fislands.classes.Rank;
import me.halfquark.fislands.utilities.IslandBoundary;
import net.milkbowl.vault.economy.Economy;

public class FCommandEx implements CommandExecutor{
	
	FIslands plugin;
	FileConfiguration config;
	Economy economy;
	Config factions;
	ApplicableRegionSet set;
	List<Faction> factionList;
	IslandBoundary islandBoundary;
	
	public FCommandEx(FIslands plugin) {
		this.plugin = plugin;
		config = plugin.getConfig();
		this.economy = plugin.getEconomy();
		if(economy == null) {
			Bukkit.getLogger().log(Level.SEVERE, "[FIslands] Economy not passed to FCommandEx");
        }
		factions = new Config("factions.yml");
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(config.getString("msg_prefix") + config.getString("msg_accent")
					+ "You must be a player to execute this command!");
			return false;
		}
		Player pSender = (Player) sender;
		if(!cmd.getName().equalsIgnoreCase("faction"))
			return false;
		if(args.length < 1)
			return help(args, pSender);
		switch(args[0]) {
		case "create":
			if(args.length != 2)
				return help(args, pSender);
			if(Faction.fromPlayer(pSender.getUniqueId()) != null) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "You are already in a Faction. Leave your Faction to create a new one"));
				return true;
			}
			if(economy.getBalance(pSender) < config.getDouble("i_creation_price")) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "You do not have enough balance to create a faction. "
					+ "You need at least " + config.getDouble("f_creation_price") + "$"));
				return true;
			}
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.name.equalsIgnoreCase(args[1])) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "There is already a Faction with this name"));
						return true;
					}
				}
			} else {
				factionList = new ArrayList<Faction>();
			}
			ArrayList<Member> MemberList = new ArrayList<Member>();
			MemberList.add(new Member(pSender.getUniqueId()));
			Faction newFaction = new Faction(args[1], pSender.getUniqueId(), MemberList, config.getDouble("f_creation_bal"));
			economy.withdrawPlayer(pSender, config.getDouble("f_creation_price"));
			factionList.add(newFaction);
			factions.set("Factions", factionList);
			factions.save();
			Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
					+ config.getString("msg_accent")+ "The faction " + args[1] + " has been created by " + pSender.getName() ));
			return true;
		case "info":
			if(args.length < 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList == null || factionList.size() == 0) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
						+ config.getString("msg_accent")+ "There are no factions on this server"));
				break;
			}
			for(Faction faction: factionList) {
				if(faction == null)
					continue;
				if(faction.name.equalsIgnoreCase(args[1])) {
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
							+ config.getString("msg_accent") + faction.name));
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
							+ "  Owner: " + Bukkit.getOfflinePlayer(faction.owner).getName()));
					for(Rank rank: faction.ranks) {
						StringBuilder sb = new StringBuilder();
						for(Member member : faction.members) {
							if(member.rank.equals(rank)) {
								sb.append(" ");
								if(Bukkit.getOfflinePlayer(member.uuid).isOnline())
									sb.append(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")));
								else
									sb.append(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")));
								sb.append(Bukkit.getOfflinePlayer(member.uuid).getName());
							}
						}
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
								+ "  " + rank.name + ":" + sb.toString()));
					}
					
					StringBuilder sb = new StringBuilder();
					for(Member member : faction.members) {
						if(member.rank == null) {
							sb.append(" ");
							if(Bukkit.getOfflinePlayer(member.uuid).isOnline())
								sb.append(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")));
							else
								sb.append(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")));
							sb.append(Bukkit.getOfflinePlayer(member.uuid).getName());
							continue;
						}
						if(member.rank.name.equals("null")) {
							sb.append(" ");
							if(Bukkit.getOfflinePlayer(member.uuid).isOnline())
								sb.append(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")));
							else
								sb.append(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")));
							sb.append(Bukkit.getOfflinePlayer(member.uuid).getName());
						}
					}
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
							+ "  No rank:" + sb.toString()));
					
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_accent")
							+ "  Balance: " + faction.balance + "$"));
					sb = new StringBuilder();
					for(String string : faction.allies) {
						sb.append(" ");
						sb.append(string);
					}
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
							+ "  Allies:" + sb.toString()));
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
				+ config.getString("msg_accent")+ "There is no faction with this name"));
			break;
		case "list":
			Integer page;
			if(args.length <= 1) {
				page = 1;
			}else {
				try {
					page = Integer.parseInt(args[1]);
				} catch(NumberFormatException e) {
					page = 1;
				}
			}
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList == null || factionList.size() == 0) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
						+ config.getString("msg_accent")+ "There are no factions on this server"));
				return true;
			}
			if(page > factionList.size() / 10 + 1)
				page = factionList.size() / 10 + 1;
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix")
					+ config.getString("msg_accent") + "Faction list " + config.getString("msg_primary") + "[page:" + page + "/" + (factionList.size() / 10 + 1) + "]"));
			for(Integer i = 10 * (page - 1); i < factionList.size() && i < 10* page; i++) {
				if(factionList.get(i) == null)
					continue;
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', 
						config.getString("msg_primary") + "  " + factionList.get(i).name));
			}
			return true;
		case "disband":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.name.equalsIgnoreCase(args[1])) {
						if(!faction.owner.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Faction's owner"));
							return true;
						}
						economy.depositPlayer(pSender, faction.balance);
						factionList.remove(faction);
						Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Faction " + faction.name + " has been disbanded"));
						factions.set("Factions", factionList);
						factions.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Faction with this name"));
			break;
		case "transfer":
			if(args.length != 3)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.name.equalsIgnoreCase(args[1])) {
						if(!faction.owner.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not this Faction's owner"));
							return true;
						}
						OfflinePlayer newOg = Bukkit.getOfflinePlayer(args[2]);
						if(!newOg.hasPlayedBefore()) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "This player hasn't joined the server"));
							return true;
						}
						faction.owner = newOg.getUniqueId();
						faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ pSender.getName() + " has transfered the Faction's ownership to " + newOg.getName()));
						factions.set("Factions", factionList);
						factions.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Faction with this name"));
			break;
		case "deposit":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) != null) {
						if(!faction.getMember(pSender.getUniqueId()).hasPermission("deposit") && !faction.owner.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You do not have rank permissions to do this"));
							return true;
						}
						Double money;
						try {
							money = Double.parseDouble(args[1]);
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
						faction.balance += money;
						faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ pSender.getName() + " has deposited " + money + "$ in the Faction's ballance"));
						factions.set("Factions", factionList);
						factions.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "withdraw":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) != null) {
						if(!faction.getMember(pSender.getUniqueId()).hasPermission("withdraw") && !faction.owner.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You do not have rank permissions to do this"));
							return true;
						}
						Double money;
						try {
							money = Double.parseDouble(args[1]);
						}catch(NumberFormatException e) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "Please specify a valid number"));
							return true;
						}
						if(faction.balance < money) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "The Faction does not have enough money"));
							return true;
						}
						economy.depositPlayer(pSender, money);
						faction.balance -= money;
						faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ pSender.getName() + " has withdrawn " + money + "$ from the Faction's ballance"));
						factions.set("Factions", factionList);
						factions.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "join":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(Faction.fromPlayer(pSender.getUniqueId()) != null) {
				pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "You are already in a Faction. Leave your Faction to accept this invite"));
				return true;
			}
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.name.equalsIgnoreCase(args[1])) {
						if(faction.invites.contains(pSender.getUniqueId())) {
							faction.invites.remove(pSender.getUniqueId());
							faction.addMember(pSender.getUniqueId());
							faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ pSender.getName() + " has joined the faction"));
							factions.set("Factions", factionList);
							factions.save();
							return true;
						}
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have a pending invite from this Faction"));
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Faction with this name"));
			break;
		case "leave":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.name.equalsIgnoreCase(args[1])) {
						if(faction.getMember(pSender.getUniqueId()) == null) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are not in this Faction"));
							return true;
						}
						if(faction.owner.equals(pSender.getUniqueId())) {
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "You are this Faction's owner. Disband or transfer ownership before leaving"));
							return true;
						}
						faction.removeMember(args[1]);
						faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ pSender.getName() + " has left the faction"));
						factions.set("Factions", factionList);
						factions.save();
						return true;
					}
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "There is no Faction with this name"));
			break;
		case "invite":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("invite") && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					if(!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "This player hasn't joined the server"));
						return true;
					}
					faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ pSender.getName() + " has invited " + Bukkit.getOfflinePlayer(args[1]).getName() + " to the faction"));
					faction.invites.add(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
					if(Bukkit.getOfflinePlayer(args[1]).isOnline())
						Bukkit.getPlayer(args[1]).sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ pSender.getName() + " has invited you to " + faction.name + ". Use /f join " + faction.name + " to join this faction"));
					factions.set("Factions", factionList);
					factions.save();
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "uninvite":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("uninvite") && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					if(faction.invites == null || faction.invites.isEmpty()) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "There are no active invites to the Faction"));
					}
					if(!faction.invites.contains(Bukkit.getOfflinePlayer(args[1]).getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "This player does not have an active invite to the Faction"));
					}
					faction.broadcast(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ pSender.getName() + " has uninvited " + Bukkit.getOfflinePlayer(args[1]).getName()));
					faction.invites.remove(Bukkit.getOfflinePlayer(args[1]).getUniqueId());
					if(Bukkit.getOfflinePlayer(args[1]).isOnline())
						Bukkit.getPlayer(args[1]).sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ pSender.getName() + " has uninvited you to " + faction.name));
					factions.set("Factions", factionList);
					factions.save();
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "newrank":
			if(args.length != 3)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("newrank") && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					ArrayList<String> permissions = new ArrayList<String>();
					permissions.addAll(Arrays.asList(args[2].split(",")));
					if(permissions == null || permissions.isEmpty()) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "Specify at least 1 permission. Format: /f newrank <rank name> [permission 1],[permission 2]..."));
						return true;
					}
					if(args[1].equalsIgnoreCase("null")) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You cannot use the name \"null\""));
						return true;
					}
					if(faction.getRank(args[1]) != null)	{
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "There exists already a rank with this name"));
						return true;
					}
					Rank newRank = new Rank(args[1], permissions);
					faction.ranks.add(newRank);
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Rank created successfully"));
					factions.set("Factions", factionList);
					factions.save();
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "delrank":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("newrank") && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					for(Rank rank : faction.ranks) {
						if(rank.name.equalsIgnoreCase(args[1])) {
							faction.ranks.remove(rank);
							for(Member member : faction.members) {
								if(member.rank.name.equalsIgnoreCase(args[1])) {
									member.setRank(new Rank("null", new ArrayList<String>()));
								}
							}
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "Rank deleted successfully"));
							factions.set("Factions", factionList);
							factions.save();
							return true;
						}
					}
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "There is no rank with this name"));
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "setrank":
			if(args.length != 3)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(faction.getRank(args[2]) == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "There is no rank with this name. Create new ranks using /f newrank"));
						return true;
					}
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("setrank:" + args[2].toLowerCase()) && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					if(faction.getMember(args[1]) == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "This player is not in the faction"));
						return true;
					}
					faction.setMemberRank(args[1], args[2]);
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Rank updated successfully"));
					factions.set("Factions", factionList);
					factions.save();
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "listperms":
			if(args.length != 1)
				return help(args, pSender);
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "Rank permissions:"));
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ "  deposit,withdraw,invite,uninvite,newrank,delrank,setrank:[rank name],ally,unally"));
			break;
		case "ally":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("ally") && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					Faction allyFaction = Faction.fromName(args[1]);
					if(allyFaction.name == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "There is no faction with this name"));
						return true;
					}
					if(faction.allies.contains(allyFaction.name)) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "This faction is already allied"));
						return true;
					}
					faction.allies.add(allyFaction.name);
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "Ally added successfully"));
					factions.set("Factions", factionList);
					factions.save();
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		case "unally":
			if(args.length != 2)
				return help(args, pSender);
			factionList = (List<Faction>) factions.getList("Factions");
			if(factionList != null && factionList.size() != 0) {
				for(Faction faction : factionList) {
					if(faction == null)
						continue;
					if(faction.getMember(pSender.getUniqueId()) == null)
						continue;
					if(!faction.getMember(pSender.getUniqueId()).hasPermission("unally") && !faction.owner.equals(pSender.getUniqueId())) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "You do not have rank permissions to do this"));
						return true;
					}
					Faction allyFaction = Faction.fromName(args[1]);
					if(allyFaction.name == null) {
						pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
							+ "There is no faction with this name"));
						return true;
					}
					for(String ally : faction.allies) {
						if(ally.equalsIgnoreCase(args[1])) {
							faction.allies.remove(ally);
							pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
								+ "Ally removed successfully"));
							factions.set("Factions", factionList);
							factions.save();
							return true;
						}
					}
					pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
						+ "This faction is not currently allied"));
					return true;
				}
			}
			pSender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
				+ "You do not belong to any faction"));
			break;
		}
		return help(args, pSender);
	}
	
	private boolean help(String[] args, Player player) {
		if(args.length > 0) {
			switch(args[0]) {
			case "create":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f create <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Create a Faction."));
				return true;
			case "info":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f info <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Display Faction info."));
				return true;
			case "list":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f list [page]"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " List all Factions on this world."));
				return true;
			case "disband":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f disband <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Disband your Faction."));
				return true;
			case "transfer":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f transfer <faction name> <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Transfer ownership of the Faction."));
				return true;
			case "deposit":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f deposit <ammount>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Deposit money on Faction's balance."));
				return true;
			case "withdraw":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f withdraw <ammount>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ " Withdraw money from Faction's balance."));
				return true;
			case "join":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f join <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Accept an invite to join a Faction"));
				return true;
			case "leave":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f leave <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Leave your Faction."));
				return true;
			case "invite":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f invite <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Invite a player to your Faction."));
				return true;
			case "uninvite":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f uninvite <player name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Uninvite a player to your Faction."));
				return true;
			case "newrank":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f newrank <rank name> [permission 1],[permission 2]..."));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Create a new rank for your Faction with the specified permissions."));
				return true;
			case "setrank":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f setrank <player name> <rank name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Assign the specified rank to a player."));
				return true;
			case "delrank":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f delrank <rank name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Delete the specified rank"));
				return true;
			case "listperms":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f listperms"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "List all rank permissions."));
				return true;
			case "ally":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f ally <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Ally another Faction."));
				return true;
			case "unally":
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
					+ "/f unally <faction name>"));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
					+ "Unally an allied Faction."));
				return true;
			}
		}
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_prefix") + config.getString("msg_accent")
			+ "/f"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " create"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " info"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " list"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " disband"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " transfer"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " deposit"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " withdraw"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " join"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " leave"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " invite"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " uninvite"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " newrank"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " setrank"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " delrank"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " listperms"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " ally"));
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("msg_primary")
				+ " unally"));
		return true;
	}
}
