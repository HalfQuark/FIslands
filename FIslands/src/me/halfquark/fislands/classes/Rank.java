package me.halfquark.fislands.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Rank implements ConfigurationSerializable {
	
	public String name;
	public ArrayList<String> permissions;
	
	public Rank(String name, ArrayList<String> permissions) {
		this.name = name;
		this.permissions = permissions;
	}
	
	@SuppressWarnings("unchecked")
	public Rank(Map<String, Object> serializedRank) {
		this.name = (String) serializedRank.get("name");
		this.permissions = (ArrayList<String>) serializedRank.get("permissions");
	}
	
	public Map<String, Object> serialize() {
		Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("peermissions", permissions);
        return result;
	}

}
