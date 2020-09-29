package com.samwolfson.spleeftime.arenas;

import org.bukkit.Location;
import org.bukkit.Material;

public enum ArenaSupplier {
    CIRCULAR_ARENA {
        @Override
        public Arena createArena(String name, Location center, Location watchLocation, Location endLocation, int size, Material floorMaterial, Material catchMaterial) {
            return new CircularArena(name, center, watchLocation, endLocation, size, floorMaterial, catchMaterial);
        }
    },

    TWO_LEVEL_CIRCULAR_ARENA {
        @Override
        public Arena createArena(String name, Location center, Location watchLocation, Location endLocation, int size, Material floorMaterial, Material catchMaterial) {
            return new TwoLevelCircularArena(name, center, watchLocation, endLocation, size, floorMaterial, catchMaterial);
        }
    };

    public abstract Arena createArena(String name, Location center, Location watchLocation, Location endLocation, int size, Material floorMaterial, Material catchMaterial);
}
