package com.samwolfson.spleeftime.arenas;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CircularArena extends Arena {

    public CircularArena(String name, Location center, Location watchLocation, Location endLocation, int size, Material floorMaterial, Material catchMaterial) {
        super(name, center, watchLocation, endLocation, size, floorMaterial, catchMaterial);
    }

    public CircularArena(Map<String, Object> serializedData) {
        super(serializedData);
    }

    @Override
    public int getFloorLevel() {
        return 6;
    }

    public int getFloorSize() {
        return size - 6;
    }

    /**
     * Evenly space players around the circular arena.
     *
     * @return a map from each player to their start location in the arena
     */
    @Override
    public Map<OfflinePlayer, Location> movePlayersToArena(List<OfflinePlayer> players) {
        Map<OfflinePlayer, Location> playerLocations = new HashMap<>();

        double distBetweenPlayers = (2 * Math.PI) / players.size();

        for (int i = 0; i < players.size(); i++) {
            // subtract 1 from the actual floor size to ensure no one falls off the edge
            double x = Math.cos(distBetweenPlayers * i) * (getFloorSize() - 1);
            double z = Math.sin(distBetweenPlayers * i) * (getFloorSize() - 1);

            Location thisPlayer = getFloorCenter().clone().add(x, 0, z);
            Vector direction = getFloorCenter().clone().subtract(thisPlayer).toVector();

            thisPlayer.setDirection(direction);   // face the center

            if (players.get(i).isOnline()) {
                ((Player) players.get(i)).teleport(thisPlayer);    // teleport to spot on arena
            }

            playerLocations.put(players.get(i), thisPlayer);
        }

        return playerLocations;
    }

    /**
     * Either generate a circle, or check whether the space for it is empty
     *
     * @param thisCenter   the center of this circle
     * @param thisMaterial material to use
     * @param checkIfEmpty whether to just check if it's empty (if true, does not generate anything)
     * @return whether or not it's empty (if checkIfEmpty is set)
     */
    protected static boolean generateOrCheckCircle(Location thisCenter, int thisSize, Material thisMaterial, boolean checkIfEmpty) {
        World world = thisCenter.getWorld();
        if (world == null) throw new IllegalArgumentException("world for arena center is null");

        int minX = thisCenter.getBlockX() - thisSize, minZ = thisCenter.getBlockZ() - thisSize, maxX = thisCenter.getBlockX() + thisSize, maxZ = thisCenter.getBlockZ() + thisSize;
        int sqRad = (int) Math.round(Math.pow(thisSize, 2));  // radius is whole, so square will also be

        for (int tx = minX; tx <= maxX; tx++) {
            for (int tz = minZ; tz < maxZ; tz++) {
                int xDist = Math.abs(tx - thisCenter.getBlockX());
                int zDist = Math.abs(tz - thisCenter.getBlockZ());
                double thisSqRad = Math.pow(xDist, 2) + Math.pow(zDist, 2);

                if (checkIfEmpty) {
                    if (thisSqRad <= sqRad) {
                        // don't actually do anything, just check if the entire arena is air
                        if (!world.getBlockAt(tx, thisCenter.getBlockY(), tz).getType().equals(Material.AIR)) {
                            return false;
                        }
                    }
                } else {
                    if (thisSqRad <= sqRad) {
                        world.getBlockAt(tx, thisCenter.getBlockY(), tz).setType(thisMaterial);
                    }
                }
            }
        }

        return true;
    }

    protected boolean generateOrCheckArena(Material floorMaterial, Material catchMaterial, boolean checkIfEmpty) {
        int minY = center.getBlockY(), maxY = center.getBlockY() + getFloorLevel() - 1;

        // loop through the cylindrical arena
        for (int ty = minY; ty <= maxY; ty++) {
            Location center = getFloorCenter().clone();
            center.setY(ty);
            if (checkIfEmpty) {
                if (ty > minY) {
                    // for > minY, check there's enough space for the arena size
                    if (!generateOrCheckCircle(center, getFloorSize(), null, true)) {
                        return false;
                    }
                } else if (ty == minY)
                    // check if there's enough space for the whole catch area
                    if (!generateOrCheckCircle(center, getOverallSize(), null, true)) {
                        return false;
                    }
            } else {
                if (ty == minY) {
                    generateOrCheckCircle(center, getOverallSize(), catchMaterial, false);
                } else if (ty == maxY) {
                    generateOrCheckCircle(center, getFloorSize(), floorMaterial, false);
                }
            }
        }

        return true;
    }

    @Override
    public void generate() {
        generateOrCheckArena(floorMaterial, catchMaterial, false);
    }

    @Override
    public void destroy() {
        generateOrCheckArena(Material.AIR, Material.AIR, false);
    }

    @Override
    public boolean isEmpty() {
        return generateOrCheckArena(null, null, true);
    }
}
