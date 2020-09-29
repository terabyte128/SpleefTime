package com.samwolfson.spleeftime.arenas;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Map;

public class TwoLevelCircularArena extends CircularArena {
    public TwoLevelCircularArena(String name, Location center, Location watchLocation, Location endLocation, int size, Material floorMaterial, Material catchMaterial) {
        super(name, center, watchLocation, endLocation, size, floorMaterial, catchMaterial);
    }

    public TwoLevelCircularArena(Map<String, Object> serializedData) {
        super(serializedData);
    }

    @Override
    public int getFloorLevel() {
        return 12;
    }

    @Override
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
                    // TODO not hardcode?
                    Material top = floorMaterial.equals(Material.AIR) ? Material.AIR : Material.GLASS;
                    generateOrCheckCircle(center, getFloorSize(), top, false);
                } else if (ty == maxY - (getFloorLevel() / 2)) {
                    generateOrCheckCircle(center, getFloorSize(), floorMaterial, false);
                }
            }
        }

        return true;
    }
}
