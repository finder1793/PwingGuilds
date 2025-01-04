package com.pwing.guilds.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import com.pwing.guilds.PwingGuilds;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public class ConfigUpdater {
    private final PwingGuilds plugin;

    public ConfigUpdater(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public void update() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
        InputStream defaultConfigStream = plugin.getResource("config.yml");

        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultConfigStream));

            Set<String> defaultKeys = defaultConfig.getKeys(true);
            for (String key : defaultKeys) {
                if (!currentConfig.contains(key)) {
                    currentConfig.set(key, defaultConfig.get(key));
                }
            }

            try {
                currentConfig.save(configFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
