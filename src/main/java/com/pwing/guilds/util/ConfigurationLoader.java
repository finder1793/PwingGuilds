package com.pwing.guilds.util;

import org.bukkit.configuration.ConfigurationSection;
import java.util.Optional;
import java.util.function.Function;

public class ConfigurationLoader {
    public static <T> T getOrDefault(ConfigurationSection config, String path, T defaultValue, Function<ConfigurationSection, T> loader) {
        return Optional.ofNullable(config.getConfigurationSection(path))
                .map(loader)
                .orElse(defaultValue);
    }
    
    public static String getString(ConfigurationSection config, String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    public static int getInt(ConfigurationSection config, String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    public static boolean getBoolean(ConfigurationSection config, String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
}
