package com.samwolfson.spleeftime.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("start")
@CommandPermission("spleeftime.play")
public class StartCommand extends BaseCommand {
    @Dependency
    SpleefTime plugin;

    @Default
    @Description("Start a match")
    public void startMatch(Player p) {
        Match match = Match.find(p);
        if (match == null) {
            p.sendMessage(ChatColor.GOLD + "You haven't created a match yet, try " + ChatColor.RESET + ChatColor.BOLD + "/match.");
            return;
        }

        match.start();
    }
}
