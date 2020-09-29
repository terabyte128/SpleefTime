package com.samwolfson.spleeftime.config;

import com.samwolfson.spleeftime.arenas.Arena;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationData implements ConfigurationSerializable {
    private Map<String, Arena> arenas;
    private Map<String, Match> matches;
    private Map<String, SpleefChest> chests;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();
        serializedData.put("arenas", arenas);
        serializedData.put("matches", matches);
        serializedData.put("chests", chests);
        return serializedData;
    }

    public ConfigurationData() {
        this.arenas = new HashMap<>();
        this.matches = new HashMap<>();
        this.chests = new HashMap<>();
    }

    public ConfigurationData(Map<String, Object> serializedData) {
        this.arenas = (Map<String, Arena>) serializedData.get("arenas");
        this.matches = (Map<String, Match>) serializedData.get("matches");
        this.chests = (Map<String, SpleefChest>) serializedData.get("chests");
    }

    public Map<String, Arena> getArenas() {
        return arenas;
    }

    public Map<String, Match> getMatches() {
        return matches;
    }

    public Map<String, SpleefChest> getChests() {
        return chests;
    }

    public String toString() {
        return "arenas: " + arenas + "\n" +
                "matches: " + matches + "\n" +
                "chests: " + chests + "\n";
    }
}
