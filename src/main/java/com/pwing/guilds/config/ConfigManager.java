package com.pwing.guilds.config;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages plugin configuration files and their loading/saving.
 * This class handles loading, saving, and reloading of all plugin configuration files.
 */
public class ConfigManager {
    private final PwingGuilds plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;

    private static final String[] CONFIG_FILES = {
        "config.yml",
        "messages.yml",
        "buffs.yml",
        "events.yml"
    };

    /**
     * Creates a new ConfigManager instance
     * @param plugin The plugin instance
     */
    public ConfigManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        loadConfigs();
    }

    /**
     * Loads all default configuration files for the plugin
     */
    private void loadConfigs() {
        for (String filename : CONFIG_FILES) {
            loadConfig(filename);
        }
    }

    /**
     * Loads a configuration file
     * @param filename The name of the file to load
     */
    public void loadConfig(String filename) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File file = new File(plugin.getDataFolder(), filename);
        
        // Always create default config if it doesn't exist
        if (!file.exists() || filename.equals("config.yml")) {
            plugin.saveResource(filename, false);
        }

        configFiles.put(filename, file);
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Load defaults from jar if they exist
        InputStream defaultStream = plugin.getResource(filename);
        if (defaultStream != null) {
            config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream)));
            config.options().copyDefaults(true);
        }
        
        configs.put(filename, config);
    }

    /**
     * Gets a configuration file
     * @param filename The name of the file
     * @return The configuration file
     */
    public FileConfiguration getConfig(String filename) {
        return configs.getOrDefault(filename, null);
    }

    /**
     * Saves a configuration file
     * @param filename The name of the file to save
     */
    public void saveConfig(String filename) {
        try {
            configs.get(filename).save(configFiles.get(filename));
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save " + filename + "!");
            e.printStackTrace();
        }
    }

    /**
     * Saves all loaded configuration files
     */
    public void saveConfigs() {
        configs.keySet().forEach(this::saveConfig);
    }

    /**
     * Reloads a specific configuration file
     * @param filename The name of the file to reload
     */
    public void reloadConfig(String filename) {
        loadConfig(filename);
    }

    /**
     * Reloads all configuration files
     */
    public void reloadAllConfigs() {
        configs.clear();
        configFiles.clear();
        loadConfigs();
    }
}
