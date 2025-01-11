package com.pwing.guilds.config;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Handles migration of configuration files between plugin versions.
 * Responsible for splitting old single-file configs into the new multi-file format.
 */
public class ConfigMigration {
    private final PwingGuilds plugin;

    /**
     * Creates a new configuration migration instance
     * @param plugin The plugin instance
     */
    public ConfigMigration(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Migrates old configuration format to new multi-file format.
     * Splits config.yml into separate files for messages, buffs, and events.
     */
    public void migrateConfigs() {
        File oldConfig = new File(plugin.getDataFolder(), "config.yml");
        if (!oldConfig.exists()) return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(oldConfig);

        // Check if this is an old single-file config
        if (config.contains("messages") || config.contains("buffs") || config.contains("events") || config.contains("gui")) {
            plugin.getLogger().info("Detected old config format - migrating to new format...");
            
            migrateSection(config, "messages", "messages.yml");
            migrateSection(config, "buffs", "buffs.yml");
            migrateSection(config, "events", "events.yml");
            migrateSection(config, "gui", "gui.yml"); // Add GUI migration
            
            // Remove old sections from main config
            config.set("messages", null);
            config.set("buffs", null);
            config.set("events", null);
            config.set("gui", null); // Remove GUI section
            
            try {
                config.save(oldConfig);
                plugin.getLogger().info("Config migration completed successfully!");
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save updated config.yml!");
                e.printStackTrace();
            }
        }

        // Ensure new config files exist
        ensureConfigExists("messages.yml");
        ensureConfigExists("buffs.yml");
        ensureConfigExists("events.yml");
        ensureConfigExists("gui.yml"); // Ensure GUI config exists
    }

    private void migrateSection(FileConfiguration oldConfig, String section, String newFile) {
        if (!oldConfig.contains(section)) return;

        File newConfigFile = new File(plugin.getDataFolder(), newFile);
        if (newConfigFile.exists()) {
            plugin.getLogger().warning(newFile + " already exists, skipping migration for " + section);
            return;
        }

        FileConfiguration newConfig = new YamlConfiguration();
        newConfig.set(section, oldConfig.get(section));

        try {
            newConfig.save(newConfigFile);
            plugin.getLogger().info("Migrated " + section + " to " + newFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + newFile);
            e.printStackTrace();
        }
    }

    private void ensureConfigExists(String filename) {
        File file = new File(plugin.getDataFolder(), filename);
        if (!file.exists()) {
            plugin.saveResource(filename, false);
            plugin.getLogger().info("Created default " + filename);
        }
    }
}
