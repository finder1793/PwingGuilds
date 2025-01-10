package com.pwing.guilds.config;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final PwingGuilds plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    public ConfigManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        loadConfigs();
    }

    private void loadConfigs() {
        loadConfig("config.yml");
        loadConfig("messages.yml");
        loadConfig("buffs.yml");
        loadConfig("events.yml");
    }

    public void loadConfig(String filename) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.saveResource(filename, false);
        }

        configFiles.put(filename, file);
        configs.put(filename, YamlConfiguration.loadConfiguration(file));
    }

    public FileConfiguration getConfig(String filename) {
        return configs.getOrDefault(filename, null);
    }

    public void saveConfig(String filename) {
        try {
            configs.get(filename).save(configFiles.get(filename));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + filename + "!");
            e.printStackTrace();
        }
    }

    public void saveConfigs() {
        configs.keySet().forEach(this::saveConfig);
    }

    public void reloadConfig(String filename) {
        loadConfig(filename);
    }

    public void reloadAllConfigs() {
        configs.clear();
        configFiles.clear();
        loadConfigs();
    }
}
