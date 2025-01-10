package com.pwing.guilds.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import com.pwing.guilds.PwingGuilds;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

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

        plugin.getLogger().info("=== Configuration Load Complete ===");

        try {
            currentConfig.save(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save updated config.yml");
            e.printStackTrace();
        }
    }
}
