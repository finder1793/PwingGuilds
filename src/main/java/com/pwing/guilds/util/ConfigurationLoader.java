package com.pwing.guilds.util;

import java.util.Optional;
import java.util.function.Function;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Utility class for loading and managing plugin configuration files
 * Handles file loading, saving, and default config creation
 */
public class ConfigurationLoader {
    
    /**
     * Private constructor to prevent instantiation of utility class
     */
    private ConfigurationLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gets a configuration value or returns a default if not found
     * @param <T> The type of value to load
     * @param config The configuration section to load from
     * @param path The path to the value
     * @param defaultValue The default value if not found
     * @param loader Function to load the value
     * @return The loaded value or default
     */
    public static <T> T getOrDefault(ConfigurationSection config, String path, T defaultValue, Function<ConfigurationSection, T> loader) {
        return Optional.ofNullable(config.getConfigurationSection(path))
                .map(loader)
                .orElse(defaultValue);
    }

    /**
     * Gets a string value from configuration
     * @param config The configuration section
     * @param path The path to the value 
     * @param defaultValue The default value if not found
     * @return The string value or default
     */
    public static String getString(ConfigurationSection config, String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }

    /**
     * Gets an integer value from configuration
     * @param config The configuration section
     * @param path The path to the value
     * @param defaultValue The default value if not found
     * @return The integer value or default
     */
    public static int getInt(ConfigurationSection config, String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    /**
     * Gets a boolean value from configuration
     * @param config The configuration section
     * @param path The path to the value
     * @param defaultValue The default value if not found
     * @return The boolean value or default
     */
    public static boolean getBoolean(ConfigurationSection config, String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
}
