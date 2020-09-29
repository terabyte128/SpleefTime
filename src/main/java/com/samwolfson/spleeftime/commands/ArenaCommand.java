package com.samwolfson.spleeftime.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.arenas.Arena;
import com.samwolfson.spleeftime.arenas.ArenaSupplier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;


@CommandAlias("arena")
@CommandPermission("spleeftime.create")
public class ArenaCommand extends BaseCommand {
    @Dependency
    private SpleefTime plugin;

    @Subcommand("create")
    @Description("Generate a new arena")
    @CommandCompletion("* @range:1-50 @blocks @arenaTypes")
    public void createArena(Player p, String name, int size, String materialString, String arenaTypeString) {
        Material arenaMaterial = Material.getMaterial(materialString);
        if (arenaMaterial == null) {
            p.sendMessage(ChatColor.GOLD + "Material not found; please try again.");
            return;
        }

        ArenaSupplier supplier;

        try {
            supplier = ArenaSupplier.valueOf(arenaTypeString);
        } catch (IllegalArgumentException ignored) {
            p.sendMessage(ChatColor.GOLD + "Arena type not found; please try again.");
            return;
        }

        // TODO make final location somewhere more reasonable
        Arena arena = supplier.createArena(name, p.getLocation(), p.getLocation(), p.getLocation(), size, arenaMaterial, Material.COBWEB);
        if (!arena.isEmpty()) {
            p.sendMessage("Arena space is not empty; please choose somewhere else.");
            return;
        }

        if (!plugin.getConfigData().getArenas().containsKey(name)) {
            plugin.getConfigData().getArenas().put(name, arena);
            plugin.saveConfigData();
            p.sendMessage(name + " was created.");
        } else {
            p.sendMessage(name + " already exists!");
        }

    }

    @Subcommand("delete")
    @Description("Remove an existing arena")
    @CommandCompletion("@arenas")
    public void deleteArena(Player p, String name) {
        if (plugin.getConfigData().getArenas().containsKey(name)) {
            plugin.getConfigData().getArenas().remove(name);
            p.sendMessage(name + " was deleted.");
        }
    }

    @Subcommand("end")
    @Description("Set the location where players go at the end of a match")
    @CommandCompletion("@arenas")
    public void setEndLocation(Player p, String name) {
        if (plugin.getConfigData().getArenas().containsKey(name)) {
            Arena arena = plugin.getConfigData().getArenas().get(name);
            arena.setEndLocation(p.getLocation());
            plugin.saveConfigData();
            p.sendMessage("End location set to current location.");
        } else {
            p.sendMessage(name + " was not found.");
        }
    }

    @Subcommand("watch")
    @Description("Set the location where players can watch from")
    @CommandCompletion("@arenas")
    public void setWatchLocation(Player p, String name) {
        if (plugin.getConfigData().getArenas().containsKey(name)) {
            Arena arena = plugin.getConfigData().getArenas().get(name);
            arena.setWatchLocation(p.getLocation());
            plugin.saveConfigData();
            p.sendMessage("Watch location set to current location.");
        } else {
            p.sendMessage(name + " was not found.");
        }
    }

    public String toString() {
        return getName() + " is a " + getClass().toString();
    }
}
