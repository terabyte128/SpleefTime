package com.samwolfson.spleeftime.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.arenas.Arena;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@CommandAlias("match")
@CommandPermission("spleeftime.play")
public class MatchCommand extends BaseCommand {
    @Dependency
    private SpleefTime plugin;

    @Default
    @Description("Create a new match")
    @CommandCompletion("@arenas @players")
    public void createMatch(Player host, String arenaName, @Split(" ") String[] players) {

        // deduplicate, convert names to Player objects, and remove non-online ones
        Set<Player> onlinePlayers = new HashSet<>();

        for (String name : players) {
            Player p = plugin.getServer().getPlayer(name);
            // TODO uncomment
            if (p != null && p.isOnline() && !p.equals(host)) {
                onlinePlayers.add(p);
            }
        }

        Arena arena = plugin.getConfigData().getArenas().get(arenaName);

        if (arena == null) {
            host.sendMessage(ChatColor.GOLD + arenaName + " does not exist; try again!");
            host.sendMessage(ChatColor.GOLD + "Options are: " + ChatColor.RESET + ChatColor.BOLD + String.join(", ", plugin.getConfigData().getArenas().keySet()));
            return;
        }

        if (Match.find(host) != null) {
            host.sendMessage(ChatColor.GOLD + "You already have an active match! Naughty boy!");
            return;
        }

        // never used here; autosaved to the plugin's config
        new Match(arena, host, onlinePlayers, true);
    }
}
