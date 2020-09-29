package com.samwolfson.spleeftime.listeners;

import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.config.Match;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;

/**
 * Listen for players in a match to fall from the sky.
 */
public class PlayerLossListener implements Listener {
    @EventHandler
    public void playerLossByFalling(PlayerMoveEvent e) {
        Map<String, Match> matches = SpleefTime.getInstance().getConfigData().getMatches();

        for (Map.Entry<String, Match> match : matches.entrySet()) {
            if (match.getValue().getPlayers().containsKey(e.getPlayer())) {
                Location playerLocation = e.getPlayer().getLocation();
                // if player falls into spiderweb
                if (playerLocation.getBlock().getType().equals(Material.COBWEB) ||
                        playerLocation.clone().add(0, 1, 0).getBlock().getType().equals(Material.COBWEB)) {

                    match.getValue().movePlayerToWatch(e.getPlayer());
                    match.getValue().defeatPlayer(e.getPlayer());
                    break;
                }
            }
        }
    }

    @EventHandler
    public void playerLossByDeath(PlayerDeathEvent e) {
        Map<String, Match> matches = SpleefTime.getInstance().getConfigData().getMatches();

        for (Map.Entry<String, Match> match : matches.entrySet()) {
            if (match.getValue().getPlayers().containsKey(e.getEntity()) && match.getValue().isStarted()) {
                e.setDeathMessage(e.getEntity().getName() + " was killed in the middle of the match!");
                match.getValue().movePlayerToWatch(e.getEntity());
                match.getValue().defeatPlayer(e.getEntity());
                break;
            }
        }
    }
}
