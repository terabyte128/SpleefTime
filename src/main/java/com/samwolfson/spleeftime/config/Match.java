package com.samwolfson.spleeftime.config;

import com.samwolfson.spleeftime.SpleefTime;
import com.samwolfson.spleeftime.arenas.Arena;
import com.samwolfson.spleeftime.tasks.MatchCountdownTask;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Match implements ConfigurationSerializable {
    private final Player host;    // the player that starts the match
    private final Arena arena;    // the arena to generate
    private final Map<OfflinePlayer, PlayerStats> players;   // all the players (including the host)
    private Map<OfflinePlayer, Location> startLocations;

    // whether or not this match should be updated in the config file whenever the relevant data change
    private final boolean autosave;
    // whether the match is started (versus waiting)
    private boolean isStarted = false;
    private int countdown = 3;

    /**
     * Create a new match.
     *
     * @param arena        An instance of the arena type to use
     * @param host         The player hosting the match
     * @param otherPlayers The other players besides the host
     * @param autosave     Whether to automatically save match data to disk.
     */
    public Match(Arena arena, Player host, Collection<Player> otherPlayers, boolean autosave) {
        this.arena = arena;
        this.host = host;
        this.autosave = autosave;

        players = new HashMap<>();
        players.put(host, new PlayerStats(host.getHealth(), host.getFoodLevel(), host.getLevel(), host.getExp(), false, true));

        // let the host know if they need to fix something with their chest
        if (!playerHasChest(host)) {
            sendMessageToHost(ChatColor.GOLD + "You need to have an empty Spleef chest before you can play.");
            sendMessageToHost(ChatColor.GOLD + "Create a double chest and place a sign on it with [Spleef] on the first line.");
        }

        for (Player p : otherPlayers) {
            players.put(p, new PlayerStats(p.getHealth(), p.getFoodLevel(), p.getLevel(), p.getExp(), false, false));
        }

        sendMessageToHost(org.bukkit.ChatColor.GREEN + "Your match was created, waiting for players to join.");
        sendInviteMessages();

        if (autosave) {
            // add this match to the plugin's config
            getPlugin().getConfigData().getMatches().put(host.getName(), this);
            getPlugin().saveConfigData();
        }
    }

    /**
     * Create a Match from serialized data.
     *
     * @param serializedData Serialized representation as returned by serialize()
     */
    public Match(Map<String, Object> serializedData) {
        this.host = (Player) serializedData.get("creator");
        this.arena = (Arena) serializedData.get("arena");
        this.players = (Map<OfflinePlayer, PlayerStats>) serializedData.get("players");
        this.autosave = (Boolean) serializedData.get("autosave");
        this.isStarted = (Boolean) serializedData.get("isStarted");
    }

    /**
     * Serialize a match
     *
     * @return A serialized representation of the match data
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> matchMap = new HashMap<>();
        matchMap.put("creator", host);
        matchMap.put("arena", arena);
        matchMap.put("players", players);
        matchMap.put("autosave", autosave);
        matchMap.put("isStarted", isStarted);
        return matchMap;
    }

    public void sendInviteMessages() {
        if (isStarted) throw new IllegalStateException("Attempted ot send invite messages to an already started match");

        players.forEach((player, playerStats) -> {
            if (!playerStats.isJoined) {
                if (player.isOnline()) {
                    Player op = (Player) player;
                    op.sendMessage(ChatColor.LIGHT_PURPLE + host.getName() + " has invited you to join a Spleef match.");
                    op.sendMessage(ChatColor.LIGHT_PURPLE + "Please type " +
                            ChatColor.RESET + ChatColor.BOLD + " /accept " + host.getName()
                            + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " or " +
                            ChatColor.RESET + ChatColor.BOLD + "/decline " + host.getName());
                }
            }
        });
    }

    /**
     * Join a player to the match (i.e., when they accept joining it).
     *
     * @param p The player to accept joining the match.
     * @return true if the player was accepted, false otherwise.
     */
    public boolean joinPlayer(Player p) {
        if (isStarted)
            throw new IllegalStateException("Attempted to join " + p.getName() + " to a match that is already started");

        if (players.containsKey(p)) {
            if (!playerHasChest(p)) {
                p.sendMessage(ChatColor.GOLD + "You need to have an empty Spleef chest before you can play.");
                p.sendMessage(ChatColor.GOLD + "Create a double chest and place a sign on it with [Spleef] on the first line.");
                p.sendMessage(ChatColor.GOLD + "Then type " + ChatColor.RESET + ChatColor.BOLD + "/accept " + host.getName() + ChatColor.RESET + ChatColor.GOLD + " again.");
                return false;
            }

            players.get(p).isJoined = true;
            save();
            p.sendMessage(ChatColor.GREEN + "You've joined the match! Sit tight while the rest of the players join.");
            sendMessageToHost(ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.GREEN + " joined your match!");
            updateHost();
            return true;
        }
        p.sendMessage(ChatColor.GOLD + "I didn't find you in this match :(");
        return false;
    }

    /**
     * Remove player from the match (i.e., if they decline).
     *
     * @param p Player to remove.
     * @return true if the player was invited and hasn't already declined, false otherwise.
     */
    public boolean removePlayer(OfflinePlayer p) {
        if (players.containsKey(p)) {
            players.remove(p);
            save();
            if (p.isOnline())
                ((Player) p).sendMessage(ChatColor.LIGHT_PURPLE + "You've declined this match.");
            sendMessageToHost(ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.LIGHT_PURPLE + " declined to join your match!");
            updateHost();
            return true;
        }
        if (p.isOnline())
            ((Player) p).sendMessage("I didn't find that match :(");
        return false;
    }

    /**
     * Check whether the match is ready to start:
     * - have all players accepted?
     * - is the arena clear?
     *
     * @return true if ready, false otherwise
     */
    public boolean isReady() {
        return playersWithoutChests().isEmpty() && allPlayersJoined() && arenaIsEmpty();
    }

    /**
     * Check that all players have either accepted or declined.
     */
    public boolean allPlayersJoined() {
        return players.values().stream().allMatch(playerStats -> playerStats.isJoined);
    }

    /**
     * Check that there are no non-air blocks in the arena.
     */
    public boolean arenaIsEmpty() {
        return arena.isEmpty();
    }

    /**
     * Check if a particular player has a chest.
     *
     * @param p Player
     */
    public boolean playerHasChest(OfflinePlayer p) {
        Map<String, SpleefChest> playerChests = getPlugin().getConfigData().getChests();
        if (!playerChests.containsKey(p.getName())) {
            return false;
        }

        String name = p.getName();
        SpleefChest chest = playerChests.get(name);
        return chest.isUsable();
    }

    /**
     * Check that all players have valid and empty spleef chests
     * Return any players that don't
     */
    public List<OfflinePlayer> playersWithoutChests() {
        List<OfflinePlayer> notReady = new ArrayList<>();
        // check that all players have a Spleef chest
        Map<String, SpleefChest> playerChests = getPlugin().getConfigData().getChests();
        for (OfflinePlayer p : players.keySet()) {
            if (!playerHasChest(p)) {
                notReady.add(p);
            }
        }
        return notReady;
    }

    public void sendMessageToHost(String message) {
        host.sendMessage(message);
    }

    /**
     * Prepare a player for Spleef:
     * - heal them and set experience to 0
     * - move all their items to their Spleef chest
     * Note: this will *not* autosave the config, callers must do so if autosave is set.
     *
     * @param p Player to prepare
     */
    private void preparePlayer(OfflinePlayer p) {
        if (isStarted)
            throw new IllegalStateException("Called preparePlayer for " + p.getName() + " on a match that is already started");

        if (!p.isOnline()) {
            removePlayer(p);
            return;
        }

        Player op = (Player) p;

        // heal player, set xp to 0
        op.setHealth(20);    // less magic-number-y way to do this?
        op.setFoodLevel(30);
        op.setExp(0);
        op.setLevel(0);

        // move all their items to their Spleef chest
        SpleefChest chest = SpleefTime.getInstance().getConfigData().getChests().get(p.getName());
        chest.getChestInventory().setContents(op.getInventory().getContents());
        op.getInventory().clear();

        // give em a shiny spoon
        op.getInventory().addItem(new ItemStack(Material.DIAMOND_SHOVEL, 1));
    }

    /**
     * Check whether the match is ready, and start it if it is.
     */
    public void start() {
        if (isStarted()) {
            sendMessageToHost(org.bukkit.ChatColor.GOLD + "This match is already started!");
        } else if (!isReady()) {
            updateHost();
        } else {
            for (OfflinePlayer p : players.keySet()) {
                preparePlayer(p);
            }

            arena.generate();
            startLocations = arena.movePlayersToArena(new ArrayList<>(players.keySet()));

            isStarted = true;
            save();
        }

        MatchCountdownTask.scheduleCountdownTask();
    }

    public boolean isCountingDown() {
        return isStarted && countdown >= 0;
    }

    public void updateCountdown() {
        for (OfflinePlayer p : players.keySet()) {
            if (!p.isOnline()) {
                return;
            }

            Player op = (Player) p;

            if (countdown == 0) {
                op.sendTitle("Spleef!", "", 10, 10, 10);
            } else {
                op.sendTitle(Integer.toString(countdown), "", 10, 30, 10);
            }
        }

        countdown--;
    }

    /**
     * Send a message to the host informing them of the match status.
     */
    public void updateHost() {
        if (allPlayersJoined()) {
            List<Player> ready = players.keySet().stream().filter(OfflinePlayer::isOnline).map(p -> (Player) p).collect(Collectors.toList());
            List<Player> notReady = playersWithoutChests().stream().filter(OfflinePlayer::isOnline).map(p -> (Player) p).collect(Collectors.toList());

            if (notReady.isEmpty()) {
                if (arenaIsEmpty()) {
                    sendMessageToHost(ChatColor.GREEN + "Your match is ready! Type /start to start it.");
                    sendMessageToHost(ChatColor.GREEN + "Participants: " + ChatColor.RESET + ChatColor.BOLD + ready.stream().map(HumanEntity::getName).collect(Collectors.joining(", ")));
                } else {
                    sendMessageToHost(ChatColor.GOLD + "There are some blocks obstructing the arena.");
                    sendMessageToHost(ChatColor.GOLD + "You'll need to remove them before starting the match!");
                }
            } else {
                sendMessageToHost(ChatColor.GOLD + "All players have joined, but not all of them have empty Spleef chests.");
                sendMessageToHost(ChatColor.GOLD + "They'll need to put a sign on a chest with [Spleef] on the first line.");
                sendMessageToHost(ChatColor.GOLD + "Players with missing or non-empty chests:");
                sendMessageToHost(ChatColor.BOLD + notReady.stream().map(HumanEntity::getName).collect(Collectors.joining(", ")));
            }
        } else {
            sendMessageToHost(ChatColor.GOLD + "You're still waiting for the following players to join:");
            sendMessageToHost(ChatColor.BOLD + players.entrySet().stream().filter(e -> !e.getValue().isJoined).map(e -> e.getKey().getName()).collect(Collectors.joining(", ")));
        }
    }

    /**
     * Destroy the arena and teleport all players to the end location, if the match was started.
     * If it was never started, just delete it and do nothing.
     */
    public void destroy() {
        // only move the players to the end of the match has started
        for (OfflinePlayer p : players.keySet()) {
            if (isStarted) {

                if (p.isOnline()) {
                    ((Player) p).teleport(arena.getEndLocation());
                }

                if (!players.get(p).isOut) {
                    restorePlayer(p);
                }
            }
        }

        arena.destroy();

        if (autosave) {
            // remove this match from the plugin config
            getPlugin().getConfigData().getMatches().remove(host.getName());
            getPlugin().saveConfigData();
        }
    }

    /**
     * Restore a player's stats and health from cached stats. Does not update the saved data.
     *
     * @param p Player
     */
    public void restorePlayer(OfflinePlayer p) {
        if (!isStarted) throw new IllegalStateException("restorePlayer called before match was started.");
        if (players.get(p).isOut) throw new IllegalStateException("Player " + p.getName() + " is already out!");

        // mark player as out
        players.get(p).isOut = true;

        // restore their items
        SpleefChest chest = SpleefTime.getInstance().getConfigData().getChests().get(p.getName());

        Inventory chestInventory = chest.getChestInventory();

        if (p.isOnline()) {
            Player op = (Player) p;

            for (int i = 0; i < 41; i++) {
                ItemStack itemStack = chestInventory.getItem(i);
                op.getInventory().setItem(i, itemStack);
                chestInventory.clear(i);
            }

            PlayerStats playerStats = players.get(p);

            op.setHealth(playerStats.health);
            op.setLevel(playerStats.level);
            op.setFoodLevel(playerStats.foodLevel);
            op.setExp((float) playerStats.exp);
        }

    }

    public void defeatPlayer(Player p) {
        if (!isStarted) throw new IllegalStateException("defeatPlayer called before match is started.");

        // restore stats and health
        restorePlayer(p);
        SpleefTime.getInstance().getServer().broadcastMessage(ChatColor.BOLD + p.getName() + ChatColor.RESET + ChatColor.AQUA + " is out!");

        // count players who aren't out yet
        List<Map.Entry<OfflinePlayer, PlayerStats>> playersLeft = players.entrySet().stream().filter(e -> !e.getValue().isOut).collect(Collectors.toList());

        // end of the game
        if (playersLeft.size() == 1) {
            // find the winner
            OfflinePlayer winner = playersLeft.get(0).getKey();

            restorePlayer(winner);
            getPlugin().getServer().broadcastMessage(ChatColor.BOLD + winner.getName() + ChatColor.RESET + ChatColor.AQUA + " has won the match!");
            destroy();
        } else if (playersLeft.isEmpty()) {
            // special case: single-player match (really only used for testing)
            sendMessageToHost(ChatColor.MAGIC + "Congrats, you played yourself.");
            destroy();
        } else {
            // not the end yet, but update config to reflect player defeated
            save();
        }
    }

    /**
     * Teleport player to the watch location.
     *
     * @param p Player
     */
    public void movePlayerToWatch(Player p) {
        p.teleport(arena.getWatchLocation());
    }

    public Map<OfflinePlayer, PlayerStats> getPlayers() {
        return players;
    }

    public Arena getArena() {
        return arena;
    }

    /**
     * Save plugin data if configured to do so.
     */
    private void save() {
        if (autosave) {
            getPlugin().saveConfigData();
        }
    }

    @Override
    public String toString() {
        return host.getName();
    }

    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Find a match created by a player.
     *
     * @param p Player
     * @return match if it exists, otherwise null
     */
    public static Match find(Player p) {
        return find(p.getName());
    }

    public static Match find(String hostName) {
        return SpleefTime.getInstance().getConfigData().getMatches().get(hostName);
    }

    private SpleefTime getPlugin() {
        return SpleefTime.getInstance();
    }

    public Optional<Map<OfflinePlayer, Location>> getStartLocations() {
        return Optional.ofNullable(startLocations);
    }

    public OfflinePlayer getHost() {
        return host;
    }

    public static class PlayerStats implements ConfigurationSerializable {
        double health;
        int level, foodLevel;
        double exp;

        boolean isOut;
        boolean isJoined;

        public PlayerStats(double health, int foodLevel, int level, double exp, boolean isOut, boolean isJoined) {
            this.health = health;
            this.foodLevel = foodLevel;
            this.level = level;
            this.exp = exp;
            this.isOut = isOut;
            this.isJoined = isJoined;
        }

        public PlayerStats(Map<String, Object> serializedData) {
            this.health = (double) serializedData.get("health");
            this.foodLevel = (int) serializedData.get("foodLevel");
            this.level = (int) serializedData.get("level");
            this.exp = (double) serializedData.get("exp");
            this.isOut = (boolean) serializedData.get("isOut");
            this.isJoined = (boolean) serializedData.get("isJoined");
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> serializedData = new HashMap<>();
            serializedData.put("health", health);
            serializedData.put("foodLevel", foodLevel);
            serializedData.put("level", level);
            serializedData.put("exp", exp);
            serializedData.put("isOut", isOut);
            serializedData.put("isJoined", isJoined);
            return serializedData;
        }
    }
}
