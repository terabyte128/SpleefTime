package com.samwolfson.spleeftime.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("cancel")
@CommandPermission("spleeftime.play")
public class CancelCommand extends BaseCommand {
    @Dependency
    SpleefTime plugin;

    @Default
    @Description("Cancel a match")
    public void cancelMatch(Player p) {
        Match match = Match.find(p);

        if (match == null) {
            p.sendMessage(ChatColor.GOLD + "You don't have an active match.");
            return;
        }

        match.destroy();
        p.sendMessage("Match cancelled.");
    }
}
