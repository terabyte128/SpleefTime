package com.samwolfson.spleeftime.listeners;

import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

/**
 * Remove player from a match when they lose connection.
 */
public class PlayerLosesConnectionListener implements Listener {
    @EventHandler
    public void playerLosesConnection(PlayerQuitEvent e) {
        removePlayer(e.getPlayer());
    }

    @EventHandler
    public void playerIsKicked(PlayerKickEvent e) {
        removePlayer(e.getPlayer());
    }

    private void removePlayer(Player p) {
        Map<String, Match> matches = SpleefTime.getInstance().getConfigData().getMatches();
        for (Map.Entry<String, Match> match : matches.entrySet()) {
            if (match.getValue().getPlayers().containsKey(p)) {
                if (match.getValue().getHost().equals(p)) {
                    match.getValue().destroy();
                } else if (match.getValue().isStarted()) {
                    p.teleport(match.getValue().getArena().getEndLocation());
                    match.getValue().defeatPlayer(p);
                } else {
                    match.getValue().removePlayer(p);
                }
            }
        }
    }
}
