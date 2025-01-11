package com.pwing.guilds.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.pwing.guilds.PwingGuilds;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates plugin configuration files to ensure required settings are present and valid.
 */
public class ConfigValidator {
    private final PwingGuilds plugin;
    private final List<String> errors = new ArrayList<>();
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Creates a new config validator
     * @param plugin Plugin instance
     */
    public ConfigValidator(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Validates the plugin configuration
     * Checks for required settings and valid values
     * @return true if configuration is valid, false otherwise
     */
    public boolean validate() {
        boolean isValid = true;

        // Validate each config file
        if (!validateMainConfig()) isValid = false;
        if (!validateEventsConfig()) isValid = false;
        if (!validateBuffsConfig()) isValid = false;
        if (!validateMessagesConfig()) isValid = false;
        if (!validateGuiConfig()) isValid = false; // Add GUI config validation

        if (!errors.isEmpty()) {
            plugin.getLogger().warning("=== Configuration Warnings ===");
            errors.forEach(error -> plugin.getLogger().warning(error));
            return false;
        }

        return isValid;
    }

    private boolean validateMainConfig() {
        FileConfiguration config = plugin.getConfig();
        
        // Validate storage settings
        if (!validateStorageSection(config)) return false;
        
        // Validate guild levels
        if (!validateGuildLevelsSection(config)) return false;
        
        // Validate exp sources
        if (!validateExpSourcesSection(config)) return false;
        
        // Validate guild claims
        if (!validateGuildClaimsSection(config)) return false;
        
        // Validate backup settings
        return validateBackupSection(config);
    }

    private boolean validateEventsConfig() {
        FileConfiguration events = plugin.getConfigManager().getConfig("events.yml");
        if (events == null) {
            plugin.saveResource("events.yml", false);
            return false;
        }

        if (!events.isConfigurationSection("events")) {
            errors.add("Missing events section in events.yml");
            return false;
        }

        // Validate each event type
        ConfigurationSection eventsSection = events.getConfigurationSection("events");
        for (String eventName : eventsSection.getKeys(false)) {
            ConfigurationSection event = eventsSection.getConfigurationSection(eventName);
            if (!event.contains("enabled")) {
                errors.add("Missing enabled flag for event: " + eventName);
            }
            if (!event.contains("rewards")) {
                errors.add("Missing rewards section for event: " + eventName);
            }
        }

        // Validate event schedule
        if (!events.isConfigurationSection("event-schedule")) {
            errors.add("Missing event-schedule section in events.yml");
            return false;
        }

        return true;
    }

    private boolean validateBuffsConfig() {
        FileConfiguration buffs = plugin.getConfigManager().getConfig("buffs.yml");
        if (buffs == null) {
            plugin.saveResource("buffs.yml", false);
            return false;
        }

        if (!buffs.isConfigurationSection("settings")) {
            errors.add("Missing settings section in buffs.yml");
            return false;
        }

        if (!buffs.isConfigurationSection("buffs")) {
            errors.add("Missing buffs section in buffs.yml");
            return false;
        }

        // Validate each buff
        ConfigurationSection buffsSection = buffs.getConfigurationSection("buffs");
        for (String buffName : buffsSection.getKeys(false)) {
            validateBuff(buffName, buffsSection.getConfigurationSection(buffName));
        }

        return true;
    }

    private boolean validateMessagesConfig() {
        FileConfiguration messages = plugin.getConfigManager().getConfig("messages.yml");
        if (messages == null) {
            plugin.saveResource("messages.yml", false);
            return false;
        }

        String[] requiredSections = {
            "messages.general",
            "messages.guild",
            "messages.buffs",
            "messages.events",
            "messages.alliance",
            "messages.error"
        };

        for (String section : requiredSections) {
            if (!messages.isConfigurationSection(section)) {
                errors.add("Missing required section in messages.yml: " + section);
                return false;
            }
        }

        return true;
    }

    // Helper methods for main config validation
    private boolean validateStorageSection(FileConfiguration config) {
        ConfigurationSection storage = config.getConfigurationSection("storage");
        if (storage == null) {
            errors.add("Missing storage configuration section!");
            return false;
        }

        String type = config.getString("storage.type");
        if (type.equalsIgnoreCase("MYSQL")) {
            ConfigurationSection mysql = config.getConfigurationSection("storage.mysql");
            if (mysql == null) {
                errors.add("MySQL storage selected but configuration is missing!");
                return false;
            }

            if (mysql.getString("host", "").isEmpty()) {
                errors.add("MySQL host is not configured!");
            }
            if (mysql.getString("database", "").isEmpty()) {
                errors.add("MySQL database is not configured!");
            }
        }

        ConfigurationSection settings = storage.getConfigurationSection("settings");
        if (settings == null) {
            errors.add("Missing storage settings configuration!");
            return false;
        }

        if (!settings.contains("default-rows")) {
            errors.add("Missing default-rows in storage settings!");
        } else {
            int rows = settings.getInt("default-rows");
            if (rows < 1 || rows > 6) {
                errors.add("Storage default-rows must be between 1 and 6!");
            }
        }

        if (!settings.contains("save-interval")) {
            errors.add("Missing save-interval in storage settings!");
        }

        return true;
    }

    private boolean validateGuildLevelsSection(FileConfiguration config) {
        ConfigurationSection levels = config.getConfigurationSection("guild-levels");
        if (levels == null) {
            errors.add("Missing guild-levels section!");
            return false;
        }

        String[] requiredPerks = {
            "member-limit",
            "teleport-cooldown",
            "exp-multiplier",
            "keep-inventory",
            "home-limit",
            "storage-rows",
            "storage-access"
        };

        int previousExp = 0;
        for (String key : levels.getKeys(false)) {
            int level = Integer.parseInt(key);
            int expRequired = levels.getInt(key + ".exp-required");

            ConfigurationSection perks = levels.getConfigurationSection(key + ".perks");
            if (perks == null) {
                errors.add("Missing perks section for guild level " + key);
            } else {
                for (String perk : requiredPerks) {
                    if (!perks.contains(perk)) {
                        errors.add("Guild level " + key + " missing required perk: " + perk);
                    }
                }
            }

            if (expRequired < previousExp) {
                errors.add("Guild level " + level + " exp requirement is lower than previous level!");
            }
            previousExp = expRequired;
        }

        return true;
    }

    private boolean validateExpSourcesSection(FileConfiguration config) {
        ConfigurationSection expSources = config.getConfigurationSection("exp-sources");
        if (expSources == null) return true;

        for (String source : expSources.getKeys(false)) {
            if (!expSources.getBoolean(source + ".enabled", false)) continue;

            ConfigurationSection values = expSources.getConfigurationSection(source + ".values");
            if (values == null) {
                errors.add("Missing values for exp source: " + source);
            }
        }

        return true;
    }

    private boolean validateGuildClaimsSection(FileConfiguration config) {
        // Add your guild claims validation logic here
        return true;
    }

    private boolean validateBackupSection(FileConfiguration config) {
        // Add your backup validation logic here
        return true;
    }

    private boolean validateGuiConfig() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection guiItems = config.getConfigurationSection("gui.items");
        if (guiItems == null) {
            errors.add("Missing gui.items section in config.yml");
            return false;
        }

        String[] requiredFields = {"material", "model-data"};
        for (String item : guiItems.getKeys(false)) {
            for (String field : requiredFields) {
                if (!guiItems.contains(item + "." + field)) {
                    errors.add("GUI item " + item + " missing required field: " + field);
                }
            }
        }

        return true;
    }

    // Helper method for buff validation
    private void validateBuff(String buffName, ConfigurationSection buff) {
        String[] required = {"name", "type", "cost", "duration", "material"};
        for (String field : required) {
            if (!buff.contains(field)) {
                errors.add("Buff " + buffName + " missing required field: " + field);
            }
        }

        // Validate material
        String material = buff.getString("material");
        try {
            Material.valueOf(material.toUpperCase());
        } catch (Exception e) {
            errors.add("Invalid material for buff " + buffName + ": " + material);
        }
    }
}