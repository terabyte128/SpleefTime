package com.samwolfson.spleeftime.listeners;

import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.Optional;

/**
 * Don't allow players to move during countdown.
 */
public class CountdownListener implements Listener {
    @EventHandler
    public void playerMovesDuringCountdown(PlayerMoveEvent e) {
        Map<String, Match> matches = SpleefTime.getInstance().getConfigData().getMatches();

        for (Map.Entry<String, Match> match : matches.entrySet()) {
            Optional<Map<OfflinePlayer, Location>> startLocations = match.getValue().getStartLocations();

            if (!startLocations.isPresent())
                continue;

            if (!match.getValue().isCountingDown())
                continue;

            if (!match.getValue().getPlayers().containsKey(e.getPlayer()))
                continue;

            if (e.getTo() != null && startLocations.get().get(e.getPlayer()).distance(e.getTo()) > 1) {
                e.setCancelled(true);
            }
        }
    }
}
