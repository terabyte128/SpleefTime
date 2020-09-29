package com.samwolfson.spleeftime;

import co.aikar.commands.BukkitCommandManager;
import com.samwolfson.spleeftime.arenas.ArenaSupplier;
import com.samwolfson.spleeftime.arenas.CircularArena;
import com.samwolfson.spleeftime.arenas.TwoLevelCircularArena;
import com.samwolfson.spleeftime.commands.*;
import com.samwolfson.spleeftime.config.ConfigurationData;
import com.samwolfson.spleeftime.config.Match;
import com.samwolfson.spleeftime.config.SpleefChest;
import com.samwolfson.spleeftime.listeners.CountdownListener;
import com.samwolfson.spleeftime.listeners.PlayerLosesConnectionListener;
import com.samwolfson.spleeftime.listeners.PlayerLossListener;
import com.samwolfson.spleeftime.listeners.SpleefChestListener;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpleefTime extends JavaPlugin {

    private ConfigurationData configData;
    private static SpleefTime instance;

    public static SpleefTime getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        saveConfigData();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        ConfigurationSerialization.registerClass(ConfigurationData.class);
        ConfigurationSerialization.registerClass(Match.class);
        ConfigurationSerialization.registerClass(SpleefChest.class);
        ConfigurationSerialization.registerClass(CircularArena.class);
        ConfigurationSerialization.registerClass(TwoLevelCircularArena.class);
        ConfigurationSerialization.registerClass(Match.PlayerStats.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        BukkitCommandManager commandManager = new BukkitCommandManager(this);
        commandManager.registerCommand(new ArenaCommand());
        commandManager.registerCommand(new MatchCommand());
        commandManager.registerCommand(new AcceptCommand());
        commandManager.registerCommand(new DeclineCommand());
        commandManager.registerCommand(new StartCommand());
        commandManager.registerCommand(new CancelCommand());

        commandManager.getCommandCompletions().registerCompletion("blocks",
                c -> Stream.of(Material.values()).map(Enum::name).collect(Collectors.toList()));
        commandManager.getCommandCompletions().registerCompletion("arenas",
                c -> new ArrayList<>(getConfigData().getArenas().keySet()));
        commandManager.getCommandCompletions().registerCompletion("matches",
                c -> new ArrayList<>(getConfigData().getMatches().keySet()));
        commandManager.getCommandCompletions().registerCompletion("arenaTypes",
                c -> new ArrayList<>(Arrays.stream(ArenaSupplier.values()).map(Enum::name).collect(Collectors.toList())));

        getServer().getPluginManager().registerEvents(new SpleefChestListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLossListener(), this);
        getServer().getPluginManager().registerEvents(new CountdownListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerLosesConnectionListener(), this);

        instance = this;
    }

    public void reloadConfigData() {
        configData = getConfig().getSerializable("spleeftime", ConfigurationData.class);
        if (configData == null) {
            configData = new ConfigurationData();   // if config is empty, start with a new one
        }
    }

    public ConfigurationData getConfigData() {
        if (configData == null)
            reloadConfigData();

        return configData;
    }

    public void saveConfigData() {
        getConfig().set("spleeftime", configData);
        saveConfig();
    }
}
