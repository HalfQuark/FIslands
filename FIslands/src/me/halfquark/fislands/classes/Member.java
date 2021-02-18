package me.halfquark.fislands.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Member implements ConfigurationSerializable {
	
	public UUID uuid;
	public Rank rank;
	
	public Member(UUID uuid, Rank rank) {
		this.uuid = uuid;
		this.rank = rank;
	}
	
	public Member(UUID uuid) {
		this.uuid = uuid;
		this.rank = new Rank("null", new ArrayList<String>());
	}
	
	public Member(Map<String, Object> serializedMember) {
		this.uuid = UUID.fromString((String) serializedMember.get("uuid"));
		this.rank = (Rank) serializedMember.get("rank");
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> result = new HashMap<>();
        result.put("uuid", uuid.toString());
        result.put("rank", rank);
        return result;
	}
	
	public boolean hasPermission(String permission) {
		if(rank.permissions == null)
			return false;
		return rank.permissions.contains(permission);
	}
	
	public void setRank(Rank rank) {
		this.rank = rank;
	}

}