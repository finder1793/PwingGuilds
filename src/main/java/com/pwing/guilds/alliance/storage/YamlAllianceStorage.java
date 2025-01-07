package com.pwing.guilds.alliance.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.alliance.Alliance;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class YamlAllianceStorage implements AllianceStorage {
    private final PwingGuilds plugin;
    private final File alliancesFolder;

    public YamlAllianceStorage(PwingGuilds plugin) {
        this.plugin = plugin;
        this.alliancesFolder = new File(plugin.getDataFolder(), "alliances");
        if (!alliancesFolder.exists()) {
            alliancesFolder.mkdirs();
        }
    }

    @Override
    public void saveAlliance(Alliance alliance) {
        File allianceFile = new File(alliancesFolder, alliance.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("alliance", alliance.serialize());
        try {
            config.save(allianceFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save alliance: " + alliance.getName());
        }
    }

    @Override
    public Alliance loadAlliance(String name) {
        File allianceFile = new File(alliancesFolder, name + ".yml");
        if (!allianceFile.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(allianceFile);
        return Alliance.deserialize(config.getConfigurationSection("alliance").getValues(false), plugin);
    }

    @Override
    public Set<Alliance> loadAllAlliances() {
        Set<Alliance> alliances = new HashSet<>();
        File[] files = alliancesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                String allianceName = file.getName().replace(".yml", "");
                Alliance alliance = loadAlliance(allianceName);
                if (alliance != null) {
                    alliances.add(alliance);
                }
            }
        }
        return alliances;
    }

    @Override
    public void deleteAlliance(String name) {
        File allianceFile = new File(alliancesFolder, name + ".yml");
        if (allianceFile.exists()) {
            allianceFile.delete();
        }
    }
}
