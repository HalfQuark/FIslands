package me.halfquark.fislands.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.halfquark.fislands.FIslands;

public class Faction implements ConfigurationSerializable {
	
	public String name;
	public String prefix;
	public UUID owner;
	public ArrayList<Rank> ranks;
	public ArrayList<Member> members;
	public ArrayList<UUID> invites;
	public ArrayList<String> allies;
	public double balance;
	
	public Faction(String name, String prefix, UUID owner, ArrayList<Member> members, double balance) {
        this.name = name;
        this.prefix = prefix;
        this.owner = owner;
        this.members = members;
        this.balance = balance;
        invites = new ArrayList<UUID>();
        ranks = new ArrayList<Rank>();
        allies = new ArrayList<String>();
    }

    @SuppressWarnings("unchecked")
    public Faction(Map<String, Object> serializedFaction) {
    	this.name = (String) serializedFaction.get("name");
    	this.prefix = (String) serializedFaction.get("prefix");
    	this.owner = UUID.fromString((String) serializedFaction.get("owner"));
    	this.members = (ArrayList<Member>) serializedFaction.get("members");
    	this.balance = (double) serializedFaction.get("balance");
    	this.ranks = (ArrayList<Rank>) serializedFaction.get("ranks");
    	this.invites = (ArrayList<UUID>) serializedFaction.get("invites");
    	this.allies = (ArrayList<String>) serializedFaction.get("allies");
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("prefix", prefix);
        result.put("owner", owner.toString());
        result.put("members", members);
        result.put("balance", balance);
        result.put("ranks", ranks);
        result.put("invites", invites);
        result.put("allies", allies);
        return result;
    }
    
    public void setName(String newName) {
    	this.name = newName;
    	return;
    }
    
    public void setPrefix(String newPrefix) {
    	this.prefix = newPrefix;
    	return;
    }
    
    public void setOwner(UUID newOwner) {
    	this.owner = newOwner;
    	return;
    }
    
    public void addRank(String rankName, ArrayList<String> permissions) {
    	this.ranks.add(new Rank(rankName, permissions));
    }
    
    public Rank getRank(String rankName) {
    	if(ranks == null || ranks.size() == 0)
    		return null;
    	for(Rank rank : ranks) {
    		if(rank.name.equalsIgnoreCase(rankName))
    			return rank;
    	}
    	return null;
    }
    
    public boolean removeRank(String rankName) {
    	for(Rank fRank : ranks) {
			if(fRank.name.equalsIgnoreCase(rankName)) {
				ranks.remove(fRank);
				return true;
			}
		}
		return false;
    }
    
	public void addMember(UUID memberUuid) {
		if(ranks == null || ranks.isEmpty())
			this.members.add(new Member(memberUuid));
		else
			this.members.add(new Member(memberUuid, ranks.get(0)));
    }
	
	public boolean removeMember(String memberName) {
		if(members.size() == 0)
			return false;
		for(Member fMember : members) {
			if(Bukkit.getOfflinePlayer(fMember.uuid).getName().equalsIgnoreCase(memberName)) {
				members.remove(fMember);
				return true;
			}
		}
		return false;
	}
	
	public boolean removeMember(UUID uuid) {
		if(members.size() == 0)
			return false;
		for(Member fMember : members) {
			if(fMember.uuid.equals(uuid)) {
				members.remove(fMember);
				return true;
			}
		}
		return false;
	}
	
	public Member getMember(String memberName) {
		for(Member fMember : members) {
			if(Bukkit.getOfflinePlayer(fMember.uuid).getName().equalsIgnoreCase(memberName)) {
				return fMember;
			}
		}
		return null;
	}
	
	public Member getMember(UUID uuid) {
		for(Member fMember : members) {
			if(fMember.uuid.equals(uuid)) {
				return fMember;
			}
		}
		return null;
	}
	
	public void setMemberRank(String memberName, String rankName) {
		Rank rank = getRank(rankName);
		for(Member fMember : members) {
			if(Bukkit.getOfflinePlayer(fMember.uuid).getName().equalsIgnoreCase(memberName)) {
				fMember.rank = rank;
				return;
			}
		}
		return;
	}
    
    public void setBalance(double newBal) {
    	this.balance = newBal;
    	return;
    }
    
    public void changeBalance(double sumBal) {
    	this.balance += sumBal;
    	return;
    }
    
    public void broadcast(String message) {
    	for(Member fMember : members) {
    		if(Bukkit.getOfflinePlayer(fMember.uuid).isOnline())
    			Bukkit.getPlayer(fMember.uuid).sendMessage(message);
    	}
    }
    
    public void broadcastAlly(String message) {
    	for(Member fMember : members) {
    		if(Bukkit.getOfflinePlayer(fMember.uuid).isOnline())
    			Bukkit.getPlayer(fMember.uuid).sendMessage(message);
    	}
    	for(String ally : allies) {
    		Faction allyFaction = Faction.fromName(ally);
    		allyFaction.broadcast(message);
    	}
    }
    
    @SuppressWarnings("unchecked")
	public static Faction fromPlayer(UUID uuid) {
    	Config factions = FIslands.factionsConfig;
    	factions.reload();
		List<Faction> factionList = (List<Faction>) factions.getList("Factions");
		if(factionList == null)
			return null;
		for(Faction faction : factionList) {
			for(Member member: faction.members) {
				if(member.uuid.equals(uuid))
					return faction;
			}
		}
		return null;
    }
    
    @SuppressWarnings("unchecked")
	public static Faction fromName(String name) {
    	Config factions = FIslands.factionsConfig;
    	factions.reload();
		List<Faction> factionList = (List<Faction>) factions.getList("Factions");
		if(factionList == null)
			return null;
		for(Faction faction : factionList) {
			if(faction.name.equalsIgnoreCase(name)) {
				return faction;
			}
		}
		return null;
	}
	
}