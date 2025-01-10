package com.pwing.guilds.util;

import org.bukkit.configuration.ConfigurationSection;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class for loading and accessing configuration values with default fallbacks.
 */
public class ConfigurationLoader {

    /**
     * Gets a configuration value with a default fallback using a custom loader
     * @param config The configuration section
     * @param path The path to the value
     * @param defaultValue The default value if not found
     * @param loader The function to load the value
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
