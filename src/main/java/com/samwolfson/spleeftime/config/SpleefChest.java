package com.samwolfson.spleeftime.config;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Chests provide a storage location for players during a match.
 */
public class SpleefChest implements ConfigurationSerializable {
    private final Location location;
    private final OfflinePlayer player;

    public SpleefChest(Location location, Player player) {
        this.location = location;
        this.player = player;
    }

    public SpleefChest(Map<String, Object> serializedData) {
        this.location = (Location) serializedData.get("location");
        this.player = (OfflinePlayer) serializedData.get("player");
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serializedData = new HashMap<>();
        serializedData.put("location", location);
        serializedData.put("player", player);
        return serializedData;
    }

    /**
     * Make sure that this chest actually exists (i.e., hasn't been destroyed or moved).
     *
     * @return true if chests exists
     */
    public boolean exists() {
        return location.getBlock().getType().equals(Material.CHEST);
    }

    /**
     * Check that the chest is usable (exists, and empty).
     *
     * @return true if usable
     */
    public boolean isUsable() {
        if (exists()) {
            Chest chest = ((Chest) location.getBlock().getState());
            return chest.getInventory().isEmpty() && chest.getInventory().getSize() > 27;
        }
        return false;
    }

    public Inventory getChestInventory() {
        if (!exists()) return null;
        return ((Chest) location.getBlock().getState()).getInventory();
    }
}
