package com.pwing.guilds.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import com.pwing.guilds.PwingGuilds;


/**
 * Handles updates to the plugin's configuration files.
 * Ensures compatibility between different versions of the config format.
 */
public class ConfigUpdater {
    private final PwingGuilds plugin;

    /**
     * Creates a new ConfigUpdater instance
     * @param plugin The PwingGuilds plugin instance
     */
    public ConfigUpdater(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Updates the configuration files to the latest version.
     * Performs necessary migrations and saves updated configurations.
     */
    public void update() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);

        plugin.getLogger().info("=== Loading PwingGuilds Configuration ===");

        ConfigurationSection levels = currentConfig.getConfigurationSection("guild-levels");
        if (levels != null) {
            for (String levelKey : levels.getKeys(false)) {
                plugin.getLogger().info("Loading guild level " + levelKey + ":");
                String perksPath = "guild-levels." + levelKey + ".perks";
                ConfigurationSection perks = levels.getConfigurationSection(levelKey + ".perks");

                if (perks != null) {
                    plugin.getLogger().info("  - Member limit: " + perks.getInt("member-limit"));
                    plugin.getLogger().info("  - Teleport cooldown: " + perks.getInt("teleport-cooldown"));
                    plugin.getLogger().info("  - Exp multiplier: " + perks.getDouble("exp-multiplier"));
                    plugin.getLogger().info("  - Keep inventory: " + perks.getBoolean("keep-inventory"));
                    plugin.getLogger().info("  - Home limit: " + perks.getInt("home-limit"));
                    plugin.getLogger().info("  - Storage rows: " + perks.getInt("storage-rows"));
                    plugin.getLogger().info("  - Storage access: " + perks.getBoolean("storage-access"));
                }
            }
        }

        // Ensure GUI items section exists
        if (!currentConfig.isConfigurationSection("gui.items")) {
            currentConfig.createSection("gui.items");
        }

        // Ensure each GUI item has required fields
        ConfigurationSection guiItems = currentConfig.getConfigurationSection("gui.items");
        String[] requiredFields = {"material", "model-data"};
        for (String item : guiItems.getKeys(false)) {
            for (String field : requiredFields) {
                if (!guiItems.contains(item + "." + field)) {
                    guiItems.set(item + "." + field, getDefaultGuiItemValue(item, field));
                }
            }
        }

        // Ensure structures section exists
        if (!currentConfig.isConfigurationSection("structures")) {
            currentConfig.createSection("structures");
        }

        ConfigurationSection structures = currentConfig.getConfigurationSection("structures");
        for (String structure : structures.getKeys(false)) {
            if (!structures.contains(structure + ".schematic")) {
                structures.set(structure + ".schematic", structure + ".schem");
            }
            if (!structures.contains(structure + ".cost")) {
                structures.createSection(structure + ".cost");
            }
        }

        plugin.getLogger().info("=== Configuration Load Complete ===");

        try {
            currentConfig.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save updated config.yml");
            e.printStackTrace();
        }
    }

    private Object getDefaultGuiItemValue(String item, String field) {
        switch (field) {
            case "material":
                return "STONE"; // Default material
            case "model-data":
                return 0; // Default model data
            default:
                return null;
        }
    }
}
