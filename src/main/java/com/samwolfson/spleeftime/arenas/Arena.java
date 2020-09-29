package com.samwolfson.spleeftime.arenas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Arena implements ConfigurationSerializable {
    protected String name;
    protected Location center;
    protected int size;
    protected Material floorMaterial;   // material that the players battle on
    protected Material catchMaterial;   // material that catches players as they fall
    protected Location watchLocation;   // for dead players to spectate
    protected Location endLocation;   // where players go at the end of the match

    public Arena(String name, Location center, Location watchLocation, Location endLocation, int size, Material floorMaterial, Material catchMaterial) {
        this.name = name;
        this.center = center;
        this.endLocation = endLocation;
        this.watchLocation = watchLocation;
        this.size = size;
        this.floorMaterial = floorMaterial;
        this.catchMaterial = catchMaterial;
    }

    public Arena(Map<String, Object> serializedData) {
        this.name = (String) serializedData.get("name");
        this.center = (Location) serializedData.get("center");
        this.watchLocation = (Location) serializedData.get("watchLocation");
        this.endLocation = (Location) serializedData.get("endLocation");
        this.size = (Integer) serializedData.get("size");
        this.floorMaterial = Material.getMaterial((String) serializedData.get("floorMaterial"));
        this.catchMaterial = Material.getMaterial((String) serializedData.get("catchMaterial"));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedMap = new HashMap<>();
        serializedMap.put("name", name);
        serializedMap.put("center", center);
        serializedMap.put("endLocation", endLocation);
        serializedMap.put("watchLocation", watchLocation);
        serializedMap.put("size", size);
        serializedMap.put("floorMaterial", floorMaterial.toString());
        serializedMap.put("catchMaterial", catchMaterial.toString());

        return serializedMap;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public Location getWatchLocation() {
        return watchLocation;
    }

    public void setWatchLocation(Location watchLocation) {
        this.watchLocation = watchLocation;
    }

    public Location getFloorCenter() {
        return center.clone().add(0, getFloorLevel(), 0);
    }

    public String getName() {
        return name;
    }

    /**
     * Generate an arena.
     */
    public abstract void generate();

    /**
     * Destroy an arena.
     */
    public abstract void destroy();

    /**
     * Check whether the space for the arena is empty.
     *
     * @return
     */
    public abstract boolean isEmpty();

    /**
     * Get the level that the players should start on, relative to the x level of the arena.
     */
    public abstract int getFloorLevel();

    public int getOverallSize() {
        return size;
    }

    public abstract int getFloorSize();

    /**
     * Place all the players in their appropriate place on the arena.
     *
     * @return a map from each player to their start location in the arena
     */
    public abstract Map<OfflinePlayer, Location> movePlayersToArena(List<OfflinePlayer> players);
}
