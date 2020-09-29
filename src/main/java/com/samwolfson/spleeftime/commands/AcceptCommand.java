package com.samwolfson.spleeftime.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.entity.Player;

@CommandAlias("accept")
@CommandPermission("spleeftime.play")
public class AcceptCommand extends BaseCommand {
    @Dependency
    SpleefTime plugin;

    @Default
    @CommandCompletion("@matches")
    @Description("Accept an invite to a match.")
    public void acceptMatch(Player p, String matchName) {
        Match match = Match.find(matchName);
        if (match == null) {
            p.sendMessage(matchName + " isn't a valid match.");
            return;
        }

        match.joinPlayer(p);
    }
}
